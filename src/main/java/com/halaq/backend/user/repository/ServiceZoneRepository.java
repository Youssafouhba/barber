package com.halaq.backend.user.repository;

import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceZoneRepository extends AbstractRepository<ServiceZone, Long> {
    List<ServiceZone> findByBarberId(Long barberId);
    ServiceZone findByLatitudeAndLongitude(Double latitude, Double longitude);
    ServiceZone findByLatitudeAndLongitudeAndBarberId(Double latitude, Double longitude, Long barberId);
}


