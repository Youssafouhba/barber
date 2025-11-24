package com.halaq.backend.core.security.service.impl;

import com.halaq.backend.core.security.config.LoginAttemptProperties;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.repository.facade.core.UserDao;
import com.halaq.backend.core.security.service.facade.LoginAttemptService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptProperties loginAttemptProperties;

    private final UserDao userService;

    public LoginAttemptServiceImpl(LoginAttemptProperties loginAttemptProperties, UserDao userService) {
        this.loginAttemptProperties = loginAttemptProperties;
        this.userService = userService;
    }

    @Override
    public void loginFailed(String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            int attempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(attempts);

            // On utilise les valeurs de notre objet de configuration
            if (attempts >= loginAttemptProperties.getMaxAttempts()) {
                user.setLockoutTime(LocalDateTime.now());
            }
            userService.save(user);
        }
    }

    @Override
    public void loginSucceeded(String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            user.setLoginAttempts(0);
            user.setLockoutTime(null);
            userService.save(user);
        }
    }

    @Override
    public boolean isLocked(String username) {
        User user = userService.findByUsername(username);
        if (user == null || user.getLockoutTime() == null) {
            return false;
        }

        // On utilise les valeurs de notre objet de configuration
        long lockoutDurationSeconds = loginAttemptProperties.getLockoutDurationMinutes() * 60L;
        return user.getLockoutTime().plusSeconds(lockoutDurationSeconds).isAfter(LocalDateTime.now());
    }

    @Override
    public Long getRemainingLockoutTime(String username) {
        User user = userService.findByUsername(username);
        if (user != null && user.getLockoutTime() != null) {
            long lockoutDurationSeconds = loginAttemptProperties.getLockoutDurationMinutes() * 60L;
            LocalDateTime lockoutEndTime = user.getLockoutTime().plusSeconds(lockoutDurationSeconds);
            LocalDateTime now = LocalDateTime.now();
            if (lockoutEndTime.isAfter(now)) {
                return java.time.Duration.between(now, lockoutEndTime).getSeconds();
            }
        }
        return 0L;
    }
}
