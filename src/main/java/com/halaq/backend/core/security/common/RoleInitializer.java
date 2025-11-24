package com.halaq.backend.core.security.common;

import com.halaq.backend.core.security.common.AuthoritiesProperties;
import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.service.facade.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Ce composant s'exécute une seule fois au démarrage de l'application.
 * Son rôle est de lire les rôles définis dans la configuration
 * et de s'assurer qu'ils existent bien en base de données.
 */
@Component
public class RoleInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);

    private final RoleService roleService;
    private final com.halaq.backend.core.security.common.AuthoritiesProperties authoritiesProperties;

    // Injection des dépendances nécessaires via le constructeur.
    // Cela fonctionne car ce composant est créé après que tout le contexte soit prêt.
    public RoleInitializer(RoleService roleService, com.halaq.backend.core.security.common.AuthoritiesProperties authoritiesProperties) {
        this.roleService = roleService;
        this.authoritiesProperties = authoritiesProperties;
    }



    /**
     * Cette méthode est appelée automatiquement par Spring au démarrage.
     */
    @Override
    public void run(String... args) throws Exception {
        if (authoritiesProperties.getRoles() == null || authoritiesProperties.getRoles().isEmpty()) {
            logger.warn("Aucun rôle n'est défini dans 'app.security.roles'. L'initialisation des rôles est ignorée.");
            return;
        }

        logger.info("Démarrage de l'initialisation des rôles de sécurité...");

        for (String roleName : authoritiesProperties.getRoles()) {
            // On utilise la méthode utilitaire pour construire le nom complet (ex: "ROLE_ADMIN")
            String authority = AuthoritiesProperties.getRole(roleName);

            if (authority != null) {
                Role role = new Role();
                role.setAuthority(authority);
                role.setCreatedAt(LocalDateTime.now());

                // La méthode findOrSave s'assure de ne pas créer de doublons.
                roleService.findOrSave(role);
                logger.info("Rôle '{}' vérifié/créé avec succès.", authority);
            }
        }
        logger.info("Initialisation des rôles de sécurité terminée.");
    }
}

