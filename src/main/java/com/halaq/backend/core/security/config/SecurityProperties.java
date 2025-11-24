package com.halaq.backend.core.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.security.endpoints")
public class SecurityProperties {
    private List<String> permitAll = List.of();
    private List<String> authenticated = List.of();
    private List<RoleMapping> roles = List.of();
    // Classe interne pour représenter le mappage rôle -> chemins
    public static class RoleMapping {
        private String authority;
        private List<String> paths;

        // Getters et Setters
        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }
    // getters and setters
    public List<String> getPermitAll() {
        return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll;
    }

    public List<String> getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(List<String> authenticated) {
        this.authenticated = authenticated;
    }

    public List<RoleMapping> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleMapping> roles) {
        this.roles = roles;
    }
}
