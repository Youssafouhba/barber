package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.dto.ServiceCategoryDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceCategoryMapper extends BaseMapper<ServiceCategory, ServiceCategoryDto> {
    @Mapping(target="services", ignore = true)
    @Override
    ServiceCategoryDto toDto(ServiceCategory entity, @Context CycleAvoidingMappingContext context);
}