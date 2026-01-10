package com.halaq.backend.user.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.mapper.BarberMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class BarberConverter extends AbstractConverter<Barber, BarberDto> {

    private final BarberMapper barberMapper;

    public BarberConverter(BarberMapper barberMapper) {
        super(Barber.class, BarberDto.class);
        this.barberMapper = barberMapper;
    }

    @Override
    public Barber toItem(BarberDto dto) {
        return dto == null ? null : barberMapper.toEntity(dto, new CycleAvoidingMappingContext());
    }

    @Override
    public BarberDto toDto(Barber item) {
        return item == null ? null : barberMapper.toDto(item,new CycleAvoidingMappingContext());
    }

    @Override
    protected BarberDto mapToDtoInternal(Barber item, CycleAvoidingMappingContext context) {
        return barberMapper.toDto(item, context);
    }
}