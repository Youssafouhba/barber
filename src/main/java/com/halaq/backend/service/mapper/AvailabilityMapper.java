package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.AvailabilityDto;
import com.halaq.backend.service.entity.Availability;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper extends BaseMapper<Availability, AvailabilityDto> {
    @Mapping(target = "barber", ignore = true)
    @Override
    AvailabilityDto toDto(Availability entity, @Context CycleAvoidingMappingContext context);
}