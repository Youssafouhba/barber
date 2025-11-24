package com.halaq.backend.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {
    private Long transactionId;
    private String status;
    private String message;
    private String errorMessage;
    
    public static PaymentResultDTO success(Long transactionId, String message) {
        return PaymentResultDTO.builder()
            .transactionId(transactionId)
            .status("SUCCESS")
            .message(message)
            .build();
    }
    
    public static PaymentResultDTO failure(String errorMessage) {
        return PaymentResultDTO.builder()
            .status("FAILED")
            .errorMessage(errorMessage)
            .build();
    }
}

