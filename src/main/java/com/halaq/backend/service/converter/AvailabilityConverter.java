package com.halaq.backend.service.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.AvailabilityDto;
import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.mapper.AvailabilityMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityConverter extends AbstractConverter<Availability, AvailabilityDto> {

    private final AvailabilityMapper availabilityMapper;

    public AvailabilityConverter(AvailabilityMapper availabilityMapper) {
        super(Availability.class, AvailabilityDto.class);
        this.availabilityMapper = availabilityMapper;
    }

    @Override
    public Availability toItem(AvailabilityDto dto) {
        return dto == null ? null : availabilityMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public AvailabilityDto toDto(Availability item) {
        return item == null ? null : availabilityMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected AvailabilityDto mapToDtoInternal(Availability item, CycleAvoidingMappingContext context) {
        return availabilityMapper.toDto(item, context);
    }
}
