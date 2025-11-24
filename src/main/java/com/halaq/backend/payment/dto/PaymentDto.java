package com.halaq.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.shared.PaymentStatus;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto extends AuditBaseDto {
    private BigDecimal amount;
    private String transactionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private PaymentMethod method;
    private PaymentStatus status;
    private Long bookingId; // Use ID to avoid circular dependency with BookingDto
}