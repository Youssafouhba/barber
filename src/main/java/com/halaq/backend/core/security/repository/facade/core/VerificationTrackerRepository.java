package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.security.entity.VerificationTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTrackerRepository extends JpaRepository<VerificationTracker,Long> {
    Optional<VerificationTracker> findByUserId(Long userId);
}
