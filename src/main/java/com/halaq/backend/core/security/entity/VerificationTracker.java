package com.halaq.backend.core.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tracker")
@Getter
@Setter
public class VerificationTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Vérifications complétées
    private Boolean emailVerified = false;
    private LocalDateTime emailVerifiedAt;

    private Boolean phoneVerified = false;
    private LocalDateTime phoneVerifiedAt;

    private Boolean cinVerified = false;
    private LocalDateTime cinVerifiedAt;

    private Boolean documentsVerified = false;
    private LocalDateTime documentsVerifiedAt;

    private Boolean portfolioVerified = false;
    private LocalDateTime portfolioVerifiedAt;

    private Boolean serviceZoneSetup = false;
    private LocalDateTime serviceZoneSetupAt;

    // Tentatives et timestamps
    private Integer emailVerificationAttempts = 0;
    private LocalDateTime lastEmailAttempt;

    private Integer phoneVerificationAttempts = 0;
    private LocalDateTime lastPhoneAttempt;
    private boolean certificationVerified;
    private LocalDateTime certificationVerifiedAt;

    // Méthodes utiles
    public boolean isVerificationComplete() {
        boolean emailDone = emailVerified;
        boolean phoneDone = phoneVerified;

        // Pour les barbiers, vérifications supplémentaires
        if ("BARBER".equals(getUserRoleType())) {
            return emailDone && phoneDone && cinVerified && documentsVerified &&
                    portfolioVerified && serviceZoneSetup;
        }

        // Pour les clients, seulement email et téléphone
        return emailDone && phoneDone;
    }

    public String getCompletionPercentage() {
        int total = 2; // email + phone (minimum)
        int completed = 0;

        if (emailVerified) completed++;
        if (phoneVerified) completed++;

        if ("ROLE_BARBER".equals(getUserRoleType())) {
            total += 3; // documents + portfolio + serviceZone
            if (cinVerified) completed++;
            if (documentsVerified) completed++;
            if (serviceZoneSetup) completed++;
        }

        return completed + "/" + total;
    }

    private String getUserRoleType() {
        return user.getRoleUsers().stream()
                .map(ru -> ru.getRole().getAuthority())
                .filter(auth -> auth.contains("ROLE_BARBER") || auth.contains("ROLE_CLIENT"))
                .findFirst()
                .orElse("ROLE_CLIENT");
    }


}