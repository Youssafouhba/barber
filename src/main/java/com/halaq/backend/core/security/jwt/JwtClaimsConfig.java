package com.halaq.backend.core.security.jwt;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt.claims")
@Validated
public class JwtClaimsConfig {

    private String enabledClaims = "";
    private String auditClaims = "";

    // MODIFIÉ : Ce sont maintenant des champs d'instance
    private final Set<String> processedEnabledClaims = new HashSet<>();
    private final Set<String> processedAuditClaims = new HashSet<>();

    // Les setters sont nécessaires pour que @ConfigurationProperties fonctionne
    public void setEnabledClaims(String enabledClaims) {
        this.enabledClaims = enabledClaims;
    }

    public void setAuditClaims(String auditClaims) {
        this.auditClaims = auditClaims;
    }

    @PostConstruct
    private void init() {
        if (enabledClaims != null && !enabledClaims.isEmpty()) {
            processedEnabledClaims.addAll(
                    Stream.of(enabledClaims.split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet())
            );
        }
        if (auditClaims != null && !auditClaims.isEmpty()) {
            processedAuditClaims.addAll(
                    Stream.of(auditClaims.split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet())
            );
        }
    }

    // MODIFIÉ : Les getters sont maintenant des méthodes d'instance
    public Set<String> getAuditClaims() {
        return Collections.unmodifiableSet(processedAuditClaims);
    }

    public Set<String> getEnabledClaims() {
        return Collections.unmodifiableSet(processedEnabledClaims);
    }
}