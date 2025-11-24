package com.halaq.backend.core.security.controller.dto.response;

import com.halaq.backend.core.security.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Réponse pour l'état de vérification du compte
 * Frontend l'utilise pour afficher la progress bar et déterminer l'étape suivante
 */
@Data
@Builder
public class VerificationStatusResponse {

    // États individuels
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean cinVerified;
    private Boolean documentsVerified;
    private Boolean portfolioVerified;
    private Boolean serviceZoneSetup;

    // État global
    private AccountStatus currentStatus;
    private String completionPercentage;
    private Boolean isComplete;

    // Prochaines étapes requises
    private List<String> nextSteps;

    // Détails pour afficher le statut
    private String statusDescription;
    private String statusColor; // "green", "yellow", "red"
}
