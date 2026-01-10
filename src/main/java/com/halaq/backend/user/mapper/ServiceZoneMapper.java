package com.halaq.backend.user.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.ServiceZoneDto;
import com.halaq.backend.user.entity.ServiceZone;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceZoneMapper extends BaseMapper<ServiceZone, ServiceZoneDto> {

    @Override
    @Mapping(target = "barber", ignore = true)
    ServiceZoneDto toDto(ServiceZone entity, @Context CycleAvoidingMappingContext context);

}


