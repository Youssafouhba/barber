package com.halaq.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class StepData {
    private String location;
    private String barberShopName;
    private List<ServiceZoneDto> zones;
}
