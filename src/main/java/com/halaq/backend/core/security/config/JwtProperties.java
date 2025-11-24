package com.halaq.backend.core.security.config;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public class JwtProperties {

    @NotEmpty
    private String secretKey;

    @Positive
    private long expirationMs;

    @NotEmpty
    private String headerPrefix = "Bearer ";

    @NotEmpty
    private String headerString = "Authorization";

    // Getters and Setters

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

    public void setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
    }

    public @NotEmpty String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(@NotEmpty String headerName) {
        this.headerString = headerName;
    }
}

