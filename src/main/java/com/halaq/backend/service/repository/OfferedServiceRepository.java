package com.halaq.backend.service.repository;

import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferedServiceRepository extends AbstractRepository<OfferedService, Long> {
}