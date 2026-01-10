package com.halaq.backend.service.service.facade;

import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.criteria.OfferedServiceCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;

public interface OfferedServiceService extends IService<OfferedService, OfferedServiceCriteria> {
    List<OfferedService> findByCategoryId(Long categoryId);
}