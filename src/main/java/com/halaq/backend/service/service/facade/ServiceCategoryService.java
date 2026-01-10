package com.halaq.backend.service.service.facade;

import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.criteria.ServiceCategoryCriteria;
import com.halaq.backend.core.service.IService;

import java.util.Optional;

public interface ServiceCategoryService extends IService<ServiceCategory, ServiceCategoryCriteria> {

    Optional<ServiceCategory> findByName(String name);
}