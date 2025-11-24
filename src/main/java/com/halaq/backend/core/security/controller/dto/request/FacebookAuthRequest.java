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
public class FacebookAuthRequest {
    @NotBlank(message = "Facebook access token is required")
    private String accessToken;

    private String userType = "CLIENT";
    private boolean acceptTerms = false;
    private boolean acceptPrivacy = false;
}