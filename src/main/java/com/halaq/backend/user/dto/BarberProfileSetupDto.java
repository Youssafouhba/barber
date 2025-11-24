package com.halaq.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BarberProfileSetupDto {
    private IdCardDataDto idCardData;
    private List<DocumentDto> documents;
    private BarberDto barber;
    private Integer currentStep;
}
