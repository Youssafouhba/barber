package com.halaq.backend.service.repository;

import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends AbstractRepository<ServiceCategory, Long> {
    Optional<ServiceCategory> findByName(String name);
}