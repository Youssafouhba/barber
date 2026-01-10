package com.halaq.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.core.dto.BaseDto;
import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.shared.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto extends BaseDto {
    private Long userId;
    
    private TransactionType type;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    
    private BigDecimal amount;
    private String stripePaymentIntentId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

