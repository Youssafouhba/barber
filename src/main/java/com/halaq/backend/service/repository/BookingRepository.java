package com.halaq.backend.service.repository;

import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends AbstractRepository<Booking, Long> {

    /**
     * Finds all bookings for a specific client.
     * @param clientId The ID of the client.
     * @return A list of bookings.
     */
    List<Booking> findByClientId(Long clientId);

    /**
     * Finds all bookings for a specific barber within a given date range.
     * @param barberId The ID of the barber.
     * @param start The start of the date range.
     * @param end The end of the date range.
     * @return A list of bookings.
     */
    List<Booking> findByBarberIdAndScheduledAtBetween(Long barberId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBarberIdAndScheduledAtAfter(Long barberId, LocalDateTime now);

    List<Booking> findByBarberId(Long barberId);
}