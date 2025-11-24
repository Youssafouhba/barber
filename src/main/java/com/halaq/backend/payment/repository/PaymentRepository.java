package com.halaq.backend.payment.repository;

import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends AbstractRepository<Payment, Long> {

    /**
     * Finds a payment by its associated booking ID.
     * @param bookingId The ID of the booking.
     * @return An Optional containing the payment if found.
     */
    Optional<Payment> findByBookingId(Long bookingId);
}