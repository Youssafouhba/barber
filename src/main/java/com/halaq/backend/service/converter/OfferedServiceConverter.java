package com.halaq.backend.service.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.OfferedServiceDto;
import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.mapper.OfferedServiceMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OfferedServiceConverter extends AbstractConverter<OfferedService, OfferedServiceDto> {

    private final OfferedServiceMapper offeredServiceMapper;


    public OfferedServiceConverter(OfferedServiceMapper offeredServiceMapper) {
        super(OfferedService.class, OfferedServiceDto.class);
        this.offeredServiceMapper = offeredServiceMapper;
    }

    @Override
    public OfferedService toItem(OfferedServiceDto dto) {
        return dto == null ? null : offeredServiceMapper.toEntity(dto, new CycleAvoidingMappingContext());
    }

    @Override
    public OfferedServiceDto toDto(OfferedService item) {
        return item == null ? null : offeredServiceMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected OfferedServiceDto mapToDtoInternal(OfferedService item, CycleAvoidingMappingContext context) {
        return offeredServiceMapper.toDto(item, context);
    }
}