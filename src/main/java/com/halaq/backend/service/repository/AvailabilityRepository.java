package com.halaq.backend.service.repository;

import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityRepository extends AbstractRepository<Availability, Long> {
    List<Availability> findByBarberId(Long barberId);
}