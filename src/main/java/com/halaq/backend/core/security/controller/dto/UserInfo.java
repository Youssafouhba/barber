package com.halaq.backend.core.security.controller.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO contenant les informations essentielles de l'utilisateur
 * Utilisé dans les réponses d'authentification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends AuditBaseDto {
    private String createdOn;
    private String updatedOn;
    // Identité
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String avatar;
    private String accountCompletionPercentage;

    // Type et rôle
    private String userType; // "ROLE_BARBER" ou "ROLE_CLIENT"
    private String role; // "BARBER" ou "CLIENT"

    // État du compte
    private String status; // AccountStatus.name()
    private Boolean cinVerified;
    private Boolean documentsVerified;
    private Boolean portfolioVerified;
    private Boolean serviceZoneSetup;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean profileCompleted;
}