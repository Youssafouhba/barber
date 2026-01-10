package com.halaq.backend.core.security.service.impl;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.repository.facade.core.VerificationTrackerRepository;
import com.halaq.backend.core.security.service.facade.VerificationTrackerService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationTrackerServiceImpl implements VerificationTrackerService {

    private final VerificationTrackerRepository repository;

    public VerificationTrackerServiceImpl(VerificationTrackerRepository repository) {
        this.repository = repository;
    }

    @Override
    public VerificationTracker createTracker(User user) {
        VerificationTracker tracker = new VerificationTracker();
        tracker.setUser(user);
        return repository.save(tracker);
    }

    @Override
    public Optional<VerificationTracker> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public VerificationTracker saveTracker(VerificationTracker tracker) {
        return repository.save(tracker);
    }

    @Override
    public void update(VerificationTracker tracker) {
        repository.save(tracker);
    }
}
