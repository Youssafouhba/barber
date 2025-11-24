package com.halaq.backend.service.service.impl;

import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.criteria.ServiceCategoryCriteria;
import com.halaq.backend.service.repository.ServiceCategoryRepository;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import com.halaq.backend.service.specification.ServiceCategorySpecification;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiceCategoryServiceImpl extends AbstractServiceImpl<ServiceCategory, ServiceCategoryCriteria, ServiceCategoryRepository> implements ServiceCategoryService {

    public ServiceCategoryServiceImpl(ServiceCategoryRepository dao) {
        super(dao);
    }

    @Override
    public void configure() {
        super.configure(ServiceCategory.class, ServiceCategorySpecification.class);
    }

    @Override
    public Optional<ServiceCategory> findByName(String name) {
        return dao.findByName(name);
    }
}