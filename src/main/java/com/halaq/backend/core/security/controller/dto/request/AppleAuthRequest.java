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
public class AppleAuthRequest {
    @NotBlank(message = "Apple identity token is required")
    private String identityToken;

    @NotBlank(message = "Apple authorization code is required")
    private String authorizationCode;

    private String userType = "CLIENT";
    private boolean acceptTerms = false;
    private boolean acceptPrivacy = false;
}
