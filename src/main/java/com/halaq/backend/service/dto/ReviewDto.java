package com.halaq.backend.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.dto.ClientDto;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto extends AuditBaseDto {
    private Integer rating;
    private String comment;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private ClientDto author;
    private BarberDto barber;
    private Long bookingId; // Use ID to avoid circular dependency with BookingDto
}