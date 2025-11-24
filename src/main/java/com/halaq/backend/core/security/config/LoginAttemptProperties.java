package com.halaq.backend.core.security.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "app.security.login-attempt")
@Validated
public class LoginAttemptProperties {

    /**
     * Maximum number of failed login attempts before locking the account.
     */
    @Min(1) // Valide que la valeur est au moins 1
    private int maxAttempts = 3; // On peut définir une valeur par défaut

    /**
     * Duration in minutes for which the account will be locked.
     */
    @Min(1)
    private int lockoutDurationMinutes = 2;

    // Getters et Setters nécessaires pour que Spring Boot injecte les valeurs
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }
}