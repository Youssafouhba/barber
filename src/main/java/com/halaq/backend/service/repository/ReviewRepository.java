package com.halaq.backend.service.repository;

import com.halaq.backend.service.entity.Review;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends AbstractRepository<Review, Long> {
}