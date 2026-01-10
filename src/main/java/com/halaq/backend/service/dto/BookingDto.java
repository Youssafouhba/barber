package com.halaq.backend.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.dto.ClientDto;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingDto extends AuditBaseDto {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private Integer totalDurationInMinutes;
    private double destinationLatitude;
    private double destinationLongitude;
    private String destinationAddress;
    private BookingStatus status;

    private ClientDto client;
    private BarberDto barber;
    private List<OfferedServiceDto> services;
}