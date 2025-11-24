package com.halaq.backend.core.security.enums;

public enum AccountStatus {
    // État initial après registration
    PENDING_EMAIL_VERIFICATION("Email not verified", 1),

    // Email vérifié, mais téléphone non vérifié
    PENDING_PHONE_VERIFICATION("Phone not verified", 2),

    // Pour les barbiers uniquement - documents requis
    PENDING_CIN_VERIFICATION("CIN pending", 3),
    PENDING_SERVICE_ZONE_SETUP("Service zone pending", 4),
    PENDING_DOCUMENT_VERIFICATION("Documents pending", 5),
    PENDING_PORTFOLIO_UPLOAD("Portfolio pending", 6),


    // Compte entièrement actif
    ACTIVE("Active", 7),

    // États de blocage
    SUSPENDED("Suspended", 8),
    BANNED("Banned", 9),

    // Réactivation après inactivité
    INACTIVE("Inactive - account dormant", 0);

    private final String description;
    private final int priority; // Pour déterminer l'étape suivante

    AccountStatus(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
}