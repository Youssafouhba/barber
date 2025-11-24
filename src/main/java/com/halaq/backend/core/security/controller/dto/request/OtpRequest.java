package com.halaq.backend.core.security.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Valid Moroccan phone number is required")
    private String phone;

    private String userType = "CLIENT"; // CLIENT, BARBER
    private String action = "LOGIN"; // LOGIN, REGISTER, VERIFY_PHONE
    private String language = "fr"; // Pour personnaliser le message SMS
}
