package com.halaq.backend.service.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.mapper.BookingMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class BookingConverter extends AbstractConverter<Booking, BookingDto> {

    private final BookingMapper bookingMapper;


    public BookingConverter(BookingMapper bookingMapper) {
        super(Booking.class, BookingDto.class);
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Booking toItem(BookingDto dto) {
        return dto == null ? null : bookingMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }


    @Override
    public BookingDto toDto(Booking item) {
        return item == null ? null : bookingMapper.toDto(item, new CycleAvoidingMappingContext(true));
    }

    @Override
    protected BookingDto mapToDtoInternal(Booking item, CycleAvoidingMappingContext context) {
        // On passe le contexte (qui contient la liste des types simples) directement au Mapper
        return item == null ? null : bookingMapper.toDto(item, context);
    }
}
