package com.halaq.backend.user.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.ServiceZoneDto;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.mapper.ServiceZoneMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ServiceZoneConverter extends AbstractConverter<ServiceZone, ServiceZoneDto> {

    private final ServiceZoneMapper mapper;

    public ServiceZoneConverter(ServiceZoneMapper mapper) {
        super(ServiceZone.class, ServiceZoneDto.class);
        this.mapper = mapper;
    }

    @Override
    public ServiceZone toItem(ServiceZoneDto dto) {
        return dto == null ? null : mapper.toEntity(dto, new CycleAvoidingMappingContext());
    }

    @Override
    public ServiceZoneDto toDto(ServiceZone item) {
        return item == null ? null : mapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected ServiceZoneDto mapToDtoInternal(ServiceZone item, CycleAvoidingMappingContext context) {
        return mapper.toDto(item, context);
    }
}


