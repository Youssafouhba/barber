package com.halaq.backend.user.service.facade;

import com.halaq.backend.core.service.IService;
import com.halaq.backend.user.criteria.ServiceZoneCriteria;
import com.halaq.backend.user.entity.ServiceZone;

import java.util.List;

public interface ServiceZoneService extends IService<ServiceZone, ServiceZoneCriteria> {
    List<ServiceZone> findByBarberId(Long barberId);

    List<ServiceZone> findOrCreate(List<ServiceZone> item);
}


