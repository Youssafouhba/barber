package com.halaq.backend.user.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.entity.Review;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.service.mapper.*; // Importez vos mappers
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.entity.Barber;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",
        uses = {DocumentMapper.class, AvailabilityMapper.class,
                ReviewMapper.class, OfferedServiceMapper.class,
                ServiceZoneMapper.class})
public abstract class BarberMapper implements BaseMapper<Barber, BarberDto> {


    @Mapping(target = "reviews", conditionExpression = "java(mapReviews(context))")
    @Mapping(target = "availability", conditionExpression = "java(mapAvailability(context))")
    @Mapping(target = "offeredServices", conditionExpression = "java(mapServices(context))")
    @Mapping(target = "bookings", conditionExpression = "java(mapBookings(context))")
    @Mapping(target = "zones", conditionExpression = "java(mapZones(context))")
    @Mapping(target = "documents", conditionExpression = "java(mapDocuments(context))")
    @Override
    public abstract BarberDto toDto(Barber entity, @Context CycleAvoidingMappingContext context);

    // --- Méthodes Helpers ---

    public boolean mapDocuments(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(Barber.class);
    }

    public boolean mapReviews(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(Review.class);
    }

    public boolean mapAvailability(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(Availability.class);
    }

    public boolean mapServices(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(OfferedService.class);
    }

    public boolean mapBookings(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(Booking.class);
    }

    public boolean mapZones(CycleAvoidingMappingContext context) {
        if (context.isSimple(Barber.class)) {
            return false; // FORCE L'ARRÊT
        }
        return context.isTypeComplex(ServiceZone.class);
    }
}