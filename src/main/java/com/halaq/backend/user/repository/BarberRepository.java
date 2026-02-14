package com.halaq.backend.user.repository;

import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.shared.BarberStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberRepository extends AbstractRepository<Barber, Long> {

    /**
     * Finds all barbers with a specific status.
     * @param status The status to filter by (e.g., ACTIVE, PENDING_VALIDATION).
     * @return A list of barbers matching the status.
     */
    List<Barber> findByStatus(BarberStatus status);

    // ✅ Trouve toutes les réservations actives d'un barbier
    // (statuts qui nécessitent le suivi en temps réel)
    @Query("SELECT b FROM Booking b WHERE b.barber.id = :barberId " +
            "AND b.status IN (com.halaq.backend.shared.BookingStatus.CONFIRMED)")
    List<Booking> findActiveBookingsByBarberId(@Param("barberId") Long barberId);

}