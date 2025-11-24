package com.halaq.backend.service.service.impl;

import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.criteria.AvailabilityCriteria;
import com.halaq.backend.service.repository.AvailabilityRepository;
import com.halaq.backend.service.service.facade.AvailabilityService;
import com.halaq.backend.service.specification.AvailabilitySpecification;
import com.halaq.backend.core.exception.BusinessRuleException;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AvailabilityServiceImpl extends AbstractServiceImpl<Availability, AvailabilityCriteria, AvailabilityRepository> implements AvailabilityService {

    public AvailabilityServiceImpl(AvailabilityRepository dao) {
        super(dao);
    }

    @Override
    @Transactional
    public Availability create(Availability availability) {
        // Business Rule: Validate that start time is before end time.
        if (availability.getStartTime() != null && availability.getEndTime() != null &&
                !availability.getStartTime().isBefore(availability.getEndTime())) {
            throw new BusinessRuleException("Start time must be before end time.");
        }

        // Business Rule: Prevent duplicate entries for the same barber on the same day.
        AvailabilityCriteria criteria = new AvailabilityCriteria();
        criteria.setBarberId(availability.getBarber().getId());
        criteria.setDay(availability.getDay());
        if (!findByCriteria(criteria).isEmpty()) {
            throw new BusinessRuleException("An availability slot for this day already exists for this barber.");
        }

        return super.create(availability);
    }

    @Override
    public List<Availability> findByBarberId(Long barberId) {
        return dao.findByBarberId(barberId);
    }

    @Override
    public void configure() {
        super.configure(Availability.class, AvailabilitySpecification.class);
    }
}