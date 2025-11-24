package com.halaq.backend.core.security.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email or username is required")
    private String login; // peut être email ou username

    @NotBlank(message = "Password is required")
    private String password;

    private String userType = "CLIENT"; // CLIENT, BARBER
    private boolean rememberMe = false;

    public @NotBlank(message = "Email or username is required") String getLogin() {
        return login;
    }

    public @NotBlank(message = "Password is required") String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }
}