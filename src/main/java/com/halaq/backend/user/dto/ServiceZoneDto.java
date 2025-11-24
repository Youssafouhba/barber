package com.halaq.backend.user.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceZoneDto extends AuditBaseDto {
    private String placeId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private BarberDto barber;
}


