package com.halaq.backend.user.service.impl;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.service.facade.VerificationTrackerService;
import com.halaq.backend.core.security.service.impl.VerificationTrackerServiceImpl;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.user.criteria.ServiceZoneCriteria;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.repository.ServiceZoneRepository;
import com.halaq.backend.user.service.facade.ServiceZoneService;
import com.halaq.backend.user.specification.ServiceZoneSpecification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Service
public class ServiceZoneServiceImpl extends AbstractServiceImpl<ServiceZone, ServiceZoneCriteria, ServiceZoneRepository> implements ServiceZoneService {

    private final VerificationTrackerService verificationTrackerService;

    public ServiceZoneServiceImpl(ServiceZoneRepository dao, VerificationTrackerService verificationTrackerService) {
        super(dao);
        this.verificationTrackerService = verificationTrackerService;
    }

    @Override
    public List<ServiceZone> findByBarberId(Long barberId) {
        return dao.findByBarberId(barberId);
    }

    @Override
    public List<ServiceZone> findOrCreate(List<ServiceZone> item) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        List<ServiceZone> results = new java.util.ArrayList<>();
        for (ServiceZone zone : item) {
            ServiceZone existingZones = findOrSave(zone);
            results.add(existingZones);
        }
        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Verification tracker not found for barber id: " + item.get(0).getBarber().getId()));
        tracker.setServiceZoneSetup(true);
        tracker.setServiceZoneSetupAt(LocalDateTime.now());
        verificationTrackerService.saveTracker(tracker);
        return results;
    }

    @Override
    public ServiceZone findOrSave(ServiceZone item) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        ServiceZone existingZone = dao.findByLatitudeAndLongitudeAndBarberId(item.getLatitude(), item.getLongitude(), user.getId());
        if (existingZone == null) {
            return super.findOrSave(item);
        } else {
            return existingZone;
        }
    }

    @Override
    public void configure() {
        super.configure(ServiceZone.class, ServiceZoneSpecification.class);
    }
}


