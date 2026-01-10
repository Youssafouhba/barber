package com.halaq.backend.service.service.facade;

import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.criteria.AvailabilityCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;

public interface AvailabilityService extends IService<Availability, AvailabilityCriteria> {

    /**
     * Finds all availability records for a specific barber.
     * @param barberId The ID of the barber.
     * @return A list of availability slots.
     */
    List<Availability> findByBarberId(Long barberId);
}