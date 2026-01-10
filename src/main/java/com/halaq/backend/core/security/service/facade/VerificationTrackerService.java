package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.entity.User;
import java.util.Optional;

public interface VerificationTrackerService {
    VerificationTracker createTracker(User user);
    Optional<VerificationTracker> findByUserId(Long userId);
    VerificationTracker saveTracker(VerificationTracker tracker);

    void update(VerificationTracker tracker);
}

