package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.service.dto.TimeBlockDto;
import com.halaq.backend.service.entity.TimeBlock;
import com.halaq.backend.user.entity.Barber;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeBlockMapper extends BaseMapper<TimeBlock, TimeBlockDto> {

    /**
     * Mapper personnalisé pour TimeBlock
     * Mappe uniquement l'ID du Barber, pas l'objet complet
     */
    @Mapping(target = "barberId", source = "barber.id")
    TimeBlockDto toDto(TimeBlock timeBlock);

    @Mapping(target = "barber.id", source = "barberId")
    TimeBlock toEntity(TimeBlockDto timeBlockDto);
}