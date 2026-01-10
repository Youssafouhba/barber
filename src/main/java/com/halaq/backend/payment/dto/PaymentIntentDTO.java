package com.halaq.backend.payment.dto;

import com.stripe.model.PaymentIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentDTO {
    private String id;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private String status;
    
    public static PaymentIntentDTO fromStripeIntent(PaymentIntent intent) {
        return PaymentIntentDTO.builder()
            .id(intent.getId())
            .clientSecret(intent.getClientSecret())
            .amount(BigDecimal.valueOf(intent.getAmount()).divide(BigDecimal.valueOf(100)))
            .currency(intent.getCurrency().toUpperCase())
            .status(intent.getStatus())
            .build();
    }
}

