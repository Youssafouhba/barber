package com.halaq.backend.service.service.impl;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.service.dto.AvailableSlotRequest;
import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.entity.TimeBlock;
import com.halaq.backend.service.repository.BookingRepository;
import com.halaq.backend.service.repository.TimeBlockRepository;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.service.specification.BookingSpecification;
import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.service.facade.BarberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Service
public class BookingServiceImpl extends AbstractServiceImpl<Booking, BookingCriteria, BookingRepository> implements BookingService {

    private final BarberService barberService;
    private final TimeBlockRepository timeBlockRepository;
    private final com.halaq.backend.notification.service.NotificationService notificationService;

    public BookingServiceImpl(BookingRepository dao, BarberService barberService, TimeBlockRepository timeBlockRepository, com.halaq.backend.notification.service.NotificationService notificationService) {
        super(dao);
        this.barberService = barberService;
        this.timeBlockRepository = timeBlockRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void configure() {
        super.configure(Booking.class, BookingSpecification.class);
    }

    // --- LOGIQUE DE GESTION DES RÉSERVATIONS PAR LE COIFFEUR (NOUVEAU) ---

    @Override
    public Booking create(Booking entity) {
        Booking booking = super.create(entity);


        // Notify Barber
        // Notify Barber
        notificationService.createNotification(
                booking.getBarber(),
                "Nouvelle réservation reçue", // Titre plus pertinent pour le barbier
                "Vous avez une nouvelle réservation avec " + booking.getClient().getFullName() +
                        " le " + booking.getScheduledAt().toLocalDate().toString() +
                        " à " + booking.getScheduledAt().toLocalTime().toString() + ".", // Ajout de la date et de l'heure pour plus de détails
                com.halaq.backend.notification.enums.NotificationType.IN_APP,
                "{\"bookingId\": " + booking.getId() + "}"
        );

        return booking;
    }

    /**
     * Permet au coiffeur d'accepter une réservation en attente.
     * @param bookingId L'ID de la réservation à accepter.
     * @return La réservation mise à jour ou null si introuvable/non autorisé.
     */
    @Override
    @Transactional
    public Booking acceptBooking(Long bookingId) {
        // 1. Trouver la réservation et vérifier sécurité
        User user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Vous n'êtes pas connecté.");
        }
        Long barberId = user.getId();

        // 1. Trouver la réservation
        Optional<Booking> optionalBooking = dao.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            return null; // Réservation non trouvée
        }

        Booking booking = optionalBooking.get();

        // 2. Vérification de sécurité et de statut
        if (!Objects.equals(booking.getBarber().getId(), barberId)) {
            // Le coiffeur n'est pas autorisé à modifier cette réservation
            throw new SecurityException("Le coiffeur n'est pas autorisé à modifier cette réservation.");
        }

        if (booking.getStatus() == BookingStatus.REQUESTED) {
            booking.setStatus(BookingStatus.CONFIRMED);

            // Notify Client
            notificationService.createNotification(
                    booking.getClient(),
                    "Booking Confirmed",
                    "Your booking with " + booking.getBarber().getFullName() + " has been confirmed.",
                    com.halaq.backend.notification.enums.NotificationType.IN_APP,
                    "{\"bookingId\": " + booking.getId() + "}"
            );

            return dao.save(booking);
        }

        // La réservation n'est pas au statut REQUESTED (déjà confirmée, annulée, etc.)
        throw new IllegalStateException("La réservation n'est pas dans un statut en attente d'acceptation.");
    }

    /**
     * Permet au coiffeur de rejeter une réservation en attente.
     * @param bookingId L'ID de la réservation à rejeter.
     * @return La réservation mise à jour ou null si introuvable/non autorisé.
     */
    @Override
    @Transactional
    public Booking rejectBooking(Long bookingId) {
        User user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Vous n'êtes pas connecté.");
        }
        Long barberId = user.getId();
        Optional<Booking> optionalBooking = dao.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            return null;
        }

        Booking booking = optionalBooking.get();

        // 2. Vérification de sécurité et de statut
        if (!Objects.equals(booking.getBarber().getId(), barberId)) {
            throw new SecurityException("Le coiffeur n'est pas autorisé à rejeter cette réservation.");
        }

        if (booking.getStatus() == BookingStatus.REQUESTED) {
            booking.setStatus(BookingStatus.REJECTED);

            // Notify Client
            notificationService.createNotification(
                    booking.getClient(),
                    "Booking Rejected",
                    "Your booking with " + booking.getBarber().getFullName() + " has been rejected.",
                    com.halaq.backend.notification.enums.NotificationType.IN_APP,
                    "{\"bookingId\": " + booking.getId() + "}"
            );

            return dao.save(booking);
        }

        throw new IllegalStateException("La réservation n'est pas dans un statut en attente de rejet.");
    }

    /**
     * Permet au coiffeur de marquer une réservation comme terminée.
     * Doit être appelé après la prestation.
     * @param bookingId L'ID de la réservation.
     * @return La réservation mise à jour.
     */
    @Override
    @Transactional
    public Booking completeBooking(Long bookingId) {
        User user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Vous n'êtes pas connecté.");
        }
        Long barberId = user.getId();
        Optional<Booking> optionalBooking = dao.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            return null;
        }

        Booking booking = optionalBooking.get();

        if (!Objects.equals(booking.getBarber().getId(), barberId)) {
            throw new SecurityException("Le coiffeur n'est pas autorisé à terminer cette réservation.");
        }

        // Seules les réservations confirmées ou en cours peuvent être terminées.
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.IN_PROGRESS) {
            booking.setStatus(BookingStatus.COMPLETED);

            // Notify Client
            notificationService.createNotification(
                    booking.getClient(),
                    "Booking Completed",
                    "Your booking with " + booking.getBarber().getFullName() + " is completed. Please rate your experience.",
                    com.halaq.backend.notification.enums.NotificationType.IN_APP,
                    "{\"bookingId\": " + booking.getId() + "}"
            );

            return dao.save(booking);
        }

        throw new IllegalStateException("La réservation n'est pas dans un statut qui permet d'être complétée.");
    }

    // --- LOGIQUE DE CALCUL DE DISPONIBILITÉ (DÉPLACÉE) ---

    /**
     * Calcule la liste des créneaux horaires disponibles pour un coiffeur donné à une date spécifique.
     * @param request Les paramètres pour calculer les créneaux disponibles.
     * @return Une liste de LocalDateTime représentant les heures de début des créneaux libres.
     */
    // J'ai renommé cette méthode en `calculateAvailableSlots` pour la distinguer des méthodes de CRUD/gestion
    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> calculateAvailableSlots(AvailableSlotRequest request) {
        Barber barber = barberService.findById(request.getBarberId());
        LocalDate date = request.getDate();
        int serviceDurationMinutes = request.getServiceDurationMinutes();

        // 1. Valider les paramètres
        if (serviceDurationMinutes <= 0 || serviceDurationMinutes > 480) { // Max 8h
            throw new IllegalArgumentException("Service duration must be between 1 and 480 minutes");
        }

        // 2. Déterminer la disponibilité théorique pour ce jour
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Availability> theoreticalAvailabilities = barber.getAvailability().stream()
                .filter(a -> a.getDay().equals(dayOfWeek))
                .toList();

        if (theoreticalAvailabilities.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Récupérer les blocs occupés (TimeBlocks + Bookings)
        List<TimeBlock> manualBlocks = timeBlockRepository.findByBarberIdAndStartDateTimeBetween(
                barber.getId(),
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );

        List<Booking> existingBookings = dao.findByBarberIdAndScheduledAtBetween(
                barber.getId(),
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );

        // 4. Fusionner et trier tous les blocs occupés
        List<TimeInterval> occupiedIntervals = mergeOccupiedIntervals(
                date,
                manualBlocks,
                existingBookings
        );

        // 5. Calculer les créneaux libres
        List<LocalDateTime> freeSlots = new ArrayList<>();

        for (Availability availableBlock : theoreticalAvailabilities) {
            LocalDateTime blockStart = LocalDateTime.of(date, availableBlock.getStartTime());
            LocalDateTime blockEnd = LocalDateTime.of(date, availableBlock.getEndTime());

            // Trouver les sous-segments libres dans ce bloc de disponibilité
            List<TimeInterval> freeSegments = calculateFreeSegments(
                    blockStart,
                    blockEnd,
                    occupiedIntervals
            );

            // Générer les créneaux à partir des segments libres
            for (TimeInterval segment : freeSegments) {
                LocalDateTime slotStart = segment.start;

                while (slotStart.plusMinutes(serviceDurationMinutes).isBefore(segment.end) ||
                        slotStart.plusMinutes(serviceDurationMinutes).isEqual(segment.end)) {
                    freeSlots.add(slotStart);
                    slotStart = slotStart.plusMinutes(serviceDurationMinutes);
                }
            }
        }

        return freeSlots;
    }

    /**
     * Fusionne et trie tous les intervalles occupés
     */
    private List<TimeInterval> mergeOccupiedIntervals(
            LocalDate date,
            List<TimeBlock> manualBlocks,
            List<Booking> existingBookings) {

        List<TimeInterval> intervals = new ArrayList<>();

        // Ajouter les TimeBlocks
        for (TimeBlock block : manualBlocks) {
            intervals.add(new TimeInterval(block.getStartDateTime(), block.getEndDateTime()));
        }

        // Ajouter les Bookings
        for (Booking booking : existingBookings) {
            LocalDateTime bookingEnd = booking.getScheduledAt()
                    .plusMinutes(booking.getTotalDurationInMinutes());
            intervals.add(new TimeInterval(booking.getScheduledAt(), bookingEnd));
        }

        // Trier par heure de début
        intervals.sort(Comparator.comparing(interval -> interval.start));

        // Fusionner les intervalles qui se chevauchent
        List<TimeInterval> merged = new ArrayList<>();

        for (TimeInterval current : intervals) {
            if (merged.isEmpty()) {
                merged.add(current);
            } else {
                TimeInterval last = merged.get(merged.size() - 1);

                // Si les intervalles se chevauchent ou se touchent, les fusionner
                if (current.start.isBefore(last.end) || current.start.isEqual(last.end)) {
                    last.end = current.end.isAfter(last.end) ? current.end : last.end;
                } else {
                    merged.add(current);
                }
            }
        }

        return merged;
    }

    /**
     * Calcule les segments libres entre un début et une fin, en excluant les intervalles occupés
     */
    private List<TimeInterval> calculateFreeSegments(
            LocalDateTime blockStart,
            LocalDateTime blockEnd,
            List<TimeInterval> occupiedIntervals) {

        List<TimeInterval> freeSegments = new ArrayList<>();
        LocalDateTime currentTime = blockStart;

        for (TimeInterval occupied : occupiedIntervals) {
            // Si l'intervalle occupé est entièrement après notre bloc, ignorer
            if (occupied.start.isAfter(blockEnd)) {
                break;
            }

            // Si l'intervalle occupé est entièrement avant notre bloc, ignorer
            if (occupied.end.isBefore(currentTime)) {
                continue;
            }

            // Ajouter le segment libre avant cet intervalle occupé
            if (currentTime.isBefore(occupied.start)) {
                freeSegments.add(new TimeInterval(
                        currentTime,
                        occupied.start.isBefore(blockEnd) ? occupied.start : blockEnd
                ));
            }

            // Avancer currentTime après cet intervalle occupé
            currentTime = occupied.end.isAfter(currentTime) ? occupied.end : currentTime;
        }

        // Ajouter le dernier segment libre s'il existe
        if (currentTime.isBefore(blockEnd)) {
            freeSegments.add(new TimeInterval(currentTime, blockEnd));
        }

        return freeSegments;
    }

    /**
     * Classe interne pour représenter un intervalle de temps
     */
    private static class TimeInterval {
        LocalDateTime start;
        LocalDateTime end;

        TimeInterval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return start + " -> " + end;
        }
    }



    @Override
    @Transactional
    // J'ai renommé l'ancienne méthode `confirmBooking` en `cancelBooking` pour éviter le conflit
    // avec la nouvelle logique d'acceptation/rejet par le coiffeur.
    public Booking cancelBooking(Long bookingId) {
        Booking booking = findById(bookingId);
        // Logic to check if cancellation is allowed (e.g., not too close to appointment time)
        if (booking != null) {
            booking.setStatus(BookingStatus.CANCELED);
            
            // Notify Barber
            notificationService.createNotification(
                    booking.getBarber(),
                    "Booking Canceled",
                    "Booking with " + booking.getClient().getFullName() + " has been canceled.",
                    com.halaq.backend.notification.enums.NotificationType.IN_APP,
                    "{\"bookingId\": " + booking.getId() + "}"
            );
            
            return dao.save(booking);
        }
        return null;
    }

    // L'ancienne méthode confirmBooking est remplacée par acceptBooking/rejectBooking et n'est plus nécessaire ici.

    @Override
    public List<Booking> findByClientId(Long clientId) {
        return dao.findByClientId(clientId);
    }

    @Override
    public List<Booking> findUpcomingBookings() {
        User user = getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        Long barberId = user.getId();
        LocalDateTime now = LocalDateTime.now();
        return dao.findByBarberIdAndScheduledAtAfter(barberId, now);
    }

    @Override
    public List<Booking> findByBarberId(Long barberId) {
        return dao.findByBarberId(barberId);
    }

    @Override
    public Booking confirmBooking(Long id) {
        Booking booking = findById(id);
        if (booking != null) {
            booking.setStatus(BookingStatus.CONFIRMED);
            
            // Notify Client
            notificationService.createNotification(
                    booking.getClient(),
                    "Booking Confirmed",
                    "Your booking with " + booking.getBarber().getFullName() + " has been confirmed.",
                    com.halaq.backend.notification.enums.NotificationType.IN_APP,
                    "{\"bookingId\": " + booking.getId() + "}"
            );
            
            return dao.save(booking);
        }
        return null;
    }
}
