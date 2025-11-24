package com.halaq.backend.service.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.ServiceCategoryDto;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.mapper.ServiceCategoryMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ServiceCategoryConverter extends AbstractConverter<ServiceCategory, ServiceCategoryDto> {

    private final ServiceCategoryMapper serviceCategoryMapper;

    public ServiceCategoryConverter(ServiceCategoryMapper serviceCategoryMapper) {
        super(ServiceCategory.class, ServiceCategoryDto.class);
        this.serviceCategoryMapper = serviceCategoryMapper;
    }

    @Override
    public ServiceCategory toItem(ServiceCategoryDto dto) {
        return dto == null ? null : serviceCategoryMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public ServiceCategoryDto toDto(ServiceCategory item) {
        return item == null ? null : serviceCategoryMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected ServiceCategoryDto mapToDtoInternal(ServiceCategory item, CycleAvoidingMappingContext context) {
        return serviceCategoryMapper.toDto(item, context);
    }
}