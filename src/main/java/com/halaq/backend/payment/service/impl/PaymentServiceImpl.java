package com.halaq.backend.payment.service.impl;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.payment.dto.PaymentIntentDTO;
import com.halaq.backend.payment.dto.PaymentResultDTO;
import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.payment.criteria.PaymentCriteria;
import com.halaq.backend.payment.repository.PaymentRepository;
import com.halaq.backend.payment.repository.TransactionRepository;
import com.halaq.backend.payment.repository.WalletRepository;
import com.halaq.backend.payment.service.AuditService;
import com.halaq.backend.payment.service.facade.PaymentService;
import com.halaq.backend.payment.specification.PaymentSpecification;
import com.halaq.backend.shared.*;
import com.halaq.backend.shared.services.StripeService;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.core.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentServiceImpl extends AbstractServiceImpl<Payment, PaymentCriteria, PaymentRepository> implements PaymentService {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private StripeService stripeService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private UserService userService;

    public PaymentServiceImpl(PaymentRepository dao) {
        super(dao);
    }

    @Override
    public void configure() {
        super.configure(Payment.class, PaymentSpecification.class);
    }

    @Override
    @Transactional
    public Payment processPaymentForBooking(Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        if (booking == null || booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Booking is not ready for payment.");
        }

        boolean paymentSuccess = true;
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(paymentSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);

        return dao.save(payment);
    }

    // 1. Recharger le portefeuille (via Stripe)
    @Transactional
    public PaymentIntentDTO rechargeWallet(Long userId, BigDecimal amount, String paymentMethodId) {
        log.info("Wallet recharge request: user={}, amount={}", userId, amount);
        
        // Validation
        if (amount.compareTo(BigDecimal.valueOf(50)) < 0 || 
            amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
            throw new IllegalArgumentException("Amount must be between 50 and 5000 MAD");
        }
        
        // Créer intent Stripe
        PaymentIntent intent = stripeService.createPaymentIntent(
            amount,
            "MAD",
            userId,
            paymentMethodId,
            "Wallet Recharge"
        );
        
        // Enregistrer transaction PENDING
        User user = userService.findById(userId);
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.RECHARGE);
        transaction.setPaymentMethod(PaymentMethod.CARD);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setAmount(amount);
        transaction.setStripePaymentIntentId(intent.getId());
        transaction.setCreatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        auditService.log(userId, "WALLET_RECHARGE_INITIATED", amount.toString());
        
        return PaymentIntentDTO.fromStripeIntent(intent);
    }
    
    // 2. Webhook Stripe - Confirmation paiement
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        Event event = stripeService.verifyAndParseWebhook(payload, signature);
        
        if (event.getType().equals("payment_intent.succeeded")) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();
            
            // Récupérer transaction
            Transaction transaction = transactionRepository
                .findByStripePaymentIntentId(intent.getId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
            
            // Mettre à jour le portefeuille
            Wallet wallet = walletRepository.findByUserId(transaction.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
            
            wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);
            
            // Marquer transaction SUCCESS
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            auditService.log(transaction.getUser().getId(), 
                "WALLET_RECHARGE_SUCCESS", 
                transaction.getAmount().toString());
        }
    }
    
    // 3. Paiement Réservation (via Wallet ou Cash)
    @Transactional
    public PaymentResultDTO processReservationPayment(
        Long bookingId,
        Long clientId,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod) {
        
        log.info("Processing reservation payment: booking={}, client={}, amount={}, method={}",
            bookingId, clientId, totalAmount, paymentMethod);
        
        Booking booking = bookingService.findById(bookingId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found");
        }
        
        if (paymentMethod == PaymentMethod.WALLET) {
            return processWalletPayment(clientId, totalAmount, bookingId);
        } else if (paymentMethod == PaymentMethod.CASH) {
            return processCashPayment(clientId, totalAmount, bookingId);
        }
        
        throw new IllegalArgumentException("Unsupported payment method");
    }
    
    @Transactional
    private PaymentResultDTO processWalletPayment(Long clientId, BigDecimal amount, Long bookingId) {
        Wallet wallet = walletRepository.findByUserId(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }
        
        // Débiter le portefeuille
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        
        // Créer transaction
        User user = userService.findById(clientId);
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(TransactionType.PAIEMENT);
        transaction.setPaymentMethod(PaymentMethod.WALLET);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        
        // Marquer réservation comme payée
        Booking booking = bookingService.findById(bookingId);
        if (booking != null) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingService.update(booking);
        }
        
        auditService.log(clientId, "RESERVATION_PAID_WALLET", amount.toString());
        
        return PaymentResultDTO.success(transaction.getId(), "Payment successful");
    }
    
    @Transactional
    private PaymentResultDTO processCashPayment(Long clientId, BigDecimal amount, Long bookingId) {
        // Vérifier si coiffeur accepte le cash
        Booking booking = bookingService.findById(bookingId);
        if (booking == null) {
            throw new EntityNotFoundException("Booking not found");
        }
        
        Transaction transaction = new Transaction();
        User user = userService.findById(clientId);
        transaction.setUser(user);
        transaction.setType(TransactionType.PAIEMENT);
        transaction.setPaymentMethod(PaymentMethod.CASH);
        transaction.setStatus(TransactionStatus.PENDING); // En attente de confirmation client/coiffeur
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        
        booking.setStatus(BookingStatus.REQUESTED);
        bookingService.update(booking);
        
        return PaymentResultDTO.success(transaction.getId(), "Cash payment awaiting confirmation");
    }
}