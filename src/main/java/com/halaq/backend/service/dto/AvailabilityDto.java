package com.halaq.backend.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilityDto extends AuditBaseDto {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private BarberDto barber;
}