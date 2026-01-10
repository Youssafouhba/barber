package com.halaq.backend.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AvailableSlotRequest {
    private Long barberId;
    private int serviceDurationMinutes;
    private LocalDate date;

}
