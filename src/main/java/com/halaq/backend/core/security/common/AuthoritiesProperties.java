package com.halaq.backend.core.security.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de configuration simple (POJO) pour contenir les propriétés de sécurité
 * lues depuis le fichier application.yml.
 * Elle n'a plus de logique métier ni de dépendances vers des services.
 */
@ConfigurationProperties(prefix = "app.security")
public class AuthoritiesProperties {

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    /**
     * Ce champ sera automatiquement rempli par Spring avec la valeur de 'app.security.roles'
     * depuis le fichier application.yml.
     */
    private List<String> roles = new ArrayList<>();

    // Getter et Setter nécessaires pour l'injection des propriétés
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Méthode utilitaire statique pour construire le nom complet d'un rôle.
     * Ex: getRole("ADMIN") -> "ROLE_ADMIN"
     * @param roleName Le nom du rôle sans le préfixe.
     * @return Le nom complet du rôle avec le préfixe.
     */
    public static String getRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return null;
        }
        return "ROLE_" + roleName.trim().toUpperCase();
    }
}
