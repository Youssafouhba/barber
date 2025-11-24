package com.halaq.backend.payment.service;

import com.halaq.backend.payment.entity.CoiffeurPayout;
import com.halaq.backend.payment.exception.PayoutFailedException;
import com.halaq.backend.payment.exception.PaymentProcessingException;
import com.halaq.backend.payment.repository.CoiffeurPayoutRepository;
import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.shared.PayoutStatus;
import com.halaq.backend.shared.services.StripeService;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.repository.BookingRepository;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.repository.BarberRepository;
import com.halaq.backend.core.exception.EntityNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PayoutService {
    
    @Autowired
    private CoiffeurPayoutRepository payoutRepository;
    
    @Autowired
    private BarberRepository barberRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private AuditService auditService;
    
    @Value("${payment.platform-commission:0.15}")
    private BigDecimal platformCommission;
    
    /**
     * Calcul hebdomadaire des versements coiffeurs
     * À exécuter chaque vendredi à 14h
     */
    @Scheduled(cron = "0 0 14 ? * FRI") // Chaque vendredi à 14h
    @Transactional
    public void processWeeklyPayouts() {
        log.info("Starting weekly payout processing...");
        
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(7);
        
        // Récupérer tous les coiffeurs
        List<Barber> barbers = barberRepository.findAll();
        
        for (Barber barber : barbers) {
            try {
                processBarberPayout(barber.getId(), startOfWeek, now);
            } catch (Exception e) {
                log.error("Error processing payout for barber {}: {}", barber.getId(), e.getMessage());
                auditService.log(barber.getId(), "PAYOUT_FAILED", e.getMessage());
            }
        }
        
        log.info("Weekly payout processing completed");
    }
    
    @Transactional
    public void processBarberPayout(Long barberId, LocalDate periodStart, LocalDate periodEnd) {
        Barber barber = barberRepository.findById(barberId)
            .orElseThrow(() -> new EntityNotFoundException("Barber not found"));
        
        // Calculer le montant total des réservations payées
        BigDecimal totalAmount = calculateBarberEarnings(barberId, periodStart, periodEnd);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No earnings for barber {} in period {}-{}", barberId, periodStart, periodEnd);
            return;
        }
        
        // Créer enregistrement payout
        CoiffeurPayout payout = new CoiffeurPayout();
        payout.setBarber(barber);
        payout.setTotalAmount(totalAmount);
        payout.setStatus(PayoutStatus.PENDING);
        payout.setPeriodStart(periodStart);
        payout.setPeriodEnd(periodEnd);
        payout.setCreatedAt(LocalDateTime.now());
        
        payoutRepository.save(payout);
        
        // Envoyer le payout
        sendPayout(payout);
        
        auditService.log(barberId, "PAYOUT_CREATED", totalAmount.toString());
    }
    
    private BigDecimal calculateBarberEarnings(Long barberId, LocalDate startDate, LocalDate endDate) {
        // Récupérer toutes les réservations complétées du coiffeur
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Booking> bookings = bookingRepository.findByBarberIdAndScheduledAtBetween(barberId, startDateTime, endDateTime);
        
        return bookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
            .map(booking -> calculateBarberAmount(booking))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateBarberAmount(Booking booking) {
        // Apply platform commission (e.g., 15%)
        BigDecimal totalPrice = booking.getTotalPrice();
        BigDecimal commission = totalPrice.multiply(platformCommission);
        return totalPrice.subtract(commission);
    }
    
    @Transactional
    private void sendPayout(CoiffeurPayout payout) {
        Barber barber = payout.getBarber();
        
        if ("STRIPE".equals(barber.getPayoutMethod())) {
            sendStripeTransfer(payout);
        } else if ("MOMO".equals(barber.getPayoutMethod())) {
            sendMomoTransfer(payout);
        }
    }
    
    private void sendStripeTransfer(CoiffeurPayout payout) {
        try {
            long amountInCents = payout.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
            String description = "Weekly payout - " + payout.getPeriodStart() + " to " + payout.getPeriodEnd();
            
            Transfer transfer = stripeService.createTransfer(
                amountInCents,
                "mad",
                payout.getBarber().getStripeAccountId(),
                description
            );
            
            payout.setStatus(PayoutStatus.PROCESSING);
            payout.setExternalReference(transfer.getId());
            payoutRepository.save(payout);
            
            log.info("Stripe transfer initiated: {} for barber {}", transfer.getId(), payout.getBarber().getId());
        } catch (PaymentProcessingException e) {
            log.error("Stripe transfer failed: {}", e.getMessage());
            payout.setStatus(PayoutStatus.FAILED);
            payoutRepository.save(payout);
            throw new PayoutFailedException("Stripe transfer failed", e);
        }
    }
    
    private void sendMomoTransfer(CoiffeurPayout payout) {
        // TODO: Implement MoMo transfer integration
        log.info("MoMo transfer not yet implemented for payout {}", payout.getId());
        payout.setStatus(PayoutStatus.FAILED);
        payoutRepository.save(payout);
        throw new PayoutFailedException("MoMo transfer not yet implemented");
    }
    
    /**
     * Vérifier le statut des payouts en attente
     */
    @Scheduled(cron = "0 */30 * * * ?") // Chaque 30 minutes
    @Transactional
    public void checkPendingPayouts() {
        List<CoiffeurPayout> pendingPayouts = payoutRepository.findByStatus(PayoutStatus.PROCESSING);
        
        for (CoiffeurPayout payout : pendingPayouts) {
            try {
                updatePayoutStatus(payout);
            } catch (Exception e) {
                log.error("Error checking payout status: {}", e.getMessage());
            }
        }
    }
    
    private void updatePayoutStatus(CoiffeurPayout payout) {
        if ("STRIPE".equals(payout.getBarber().getPayoutMethod())) {
            checkStripeTransferStatus(payout);
        } else if ("MOMO".equals(payout.getBarber().getPayoutMethod())) {
            checkMomoTransferStatus(payout);
        }
    }
    
    private void checkStripeTransferStatus(CoiffeurPayout payout) {
        try {
            Transfer transfer = stripeService.retrieveTransfer(payout.getExternalReference());
            
            PayoutStatus newStatus = PayoutStatus.COMPLETED;
            // Check if transfer failed or is still pending based on available fields
            // Note: Stripe Transfer object structure may vary, adjust based on actual API
            if (!transfer.getReversed() && transfer.getAmount() > 0) {
                newStatus = PayoutStatus.COMPLETED;
            } else if (transfer.getReversed()) {
                newStatus = PayoutStatus.FAILED;
            }
            
            payout.setStatus(newStatus);
            if (newStatus == PayoutStatus.COMPLETED) {
                payout.setProcessedAt(LocalDateTime.now());
            }
            payoutRepository.save(payout);
        } catch (StripeException e) {
            log.error("Error checking Stripe transfer status: {}", e.getMessage());
        }
    }
    
    private void checkMomoTransferStatus(CoiffeurPayout payout) {
        // TODO: Implement MoMo transfer status check
        log.info("MoMo transfer status check not yet implemented");
    }
}

