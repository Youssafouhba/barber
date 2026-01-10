package com.halaq.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.core.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WalletDto extends BaseDto {
    private Long userId;
    
    private BigDecimal balance;
    private String currency;
    
    private Long version;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

