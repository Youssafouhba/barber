package com.halaq.backend.service.converter;

import com.halaq.backend.core.converter.AbstractConverter;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.TimeBlockDto;
import com.halaq.backend.service.entity.TimeBlock;
import com.halaq.backend.service.mapper.TimeBlockMapper;
import org.springframework.stereotype.Component;

@Component
public class TimeBlockConverter extends AbstractConverter<TimeBlock, TimeBlockDto> {

    private final TimeBlockMapper timeBlockMapper;

    public TimeBlockConverter(TimeBlockMapper timeBlockMapper) {
        super(TimeBlock.class, TimeBlockDto.class);
        this.timeBlockMapper = timeBlockMapper;
    }

    @Override
    public TimeBlock toItem(TimeBlockDto dto) {
        return dto == null ? null : timeBlockMapper.toEntity(dto, new CycleAvoidingMappingContext());
    }

    @Override
    public TimeBlockDto toDto(TimeBlock item) {
        return item == null ? null : timeBlockMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected TimeBlockDto mapToDtoInternal(TimeBlock item, CycleAvoidingMappingContext context) {
        return timeBlockMapper.toDto(item, context);
    }
}


