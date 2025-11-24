package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.user.mapper.BarberMapper;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.halaq.backend.user.entity.Barber;
@Mapper(componentModel = "spring",uses = {OfferedServiceMapper.class, BarberMapper.class})
public interface BookingMapper extends BaseMapper<Booking, BookingDto> {
    //@Mapping(target = "barber", conditionExpression = "java(shouldMapBarber(context))")
    @Mapping(target = "services", conditionExpression = "java(shouldMapServices(context))")
    @Override
    BookingDto toDto(Booking entity, @Context CycleAvoidingMappingContext context);

    // Méthodes par défaut pour simplifier les expressions
    default boolean shouldMapBarber(CycleAvoidingMappingContext context) {
        return context.isTypeComplex(com.halaq.backend.user.entity.Barber.class);
    }

    default boolean shouldMapServices(CycleAvoidingMappingContext context) {
        return context.isTypeComplex(com.halaq.backend.service.entity.OfferedService.class);
    }
}