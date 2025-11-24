package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.OfferedServiceDto;
import com.halaq.backend.service.entity.OfferedService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {ServiceCategoryMapper.class})
public interface OfferedServiceMapper extends BaseMapper<OfferedService, OfferedServiceDto> {

    @Mapping(target="barber", ignore = true)
    @Override
    OfferedServiceDto toDto(OfferedService entity, @Context CycleAvoidingMappingContext context);
}