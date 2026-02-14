package com.halaq.backend.service.service.facade;

import com.halaq.backend.service.dto.AvailableSlotRequest;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.core.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService extends IService<Booking, BookingCriteria> {

    @Transactional
    Booking acceptBooking(Long bookingId);

    @Transactional
    Booking rejectBooking(Long bookingId);

    @Transactional
    Booking completeBooking(Long bookingId);

    // J'ai renommé cette méthode en `calculateAvailableSlots` pour la distinguer des méthodes de CRUD/gestion
    List<LocalDateTime> calculateAvailableSlots(AvailableSlotRequest request);

    @Transactional
    Booking cancelBooking(Long bookingId);

    List<Booking> findByClientId(Long clientId);

    List<Booking> findUpcomingBookings();

    List<Booking> findByBarberId(Long barberId);

    Booking confirmBooking(Long id);

    Booking startTrip(Long bookingId);

    Booking markArrived(Long bookingId);

    Booking startService(Long bookingId);

}