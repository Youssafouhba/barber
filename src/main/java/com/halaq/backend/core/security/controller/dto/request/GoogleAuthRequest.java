package com.halaq.backend.core.security.controller.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    private String userType = "CLIENT";
    private boolean acceptTerms = false;
    private boolean acceptPrivacy = false;
}
