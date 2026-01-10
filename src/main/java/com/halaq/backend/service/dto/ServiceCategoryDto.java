package com.halaq.backend.service.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCategoryDto extends AuditBaseDto {
    private String name;
    private String iconUrl;
    private OfferedServiceDto[] services;
}