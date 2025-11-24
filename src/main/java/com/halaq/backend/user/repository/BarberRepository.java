package com.halaq.backend.user.repository;

import com.halaq.backend.shared.BarberStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.core.repository.AbstractRepository;
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
}