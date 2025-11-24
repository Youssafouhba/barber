package com.halaq.backend.core.mapper;

import com.halaq.backend.core.dto.BaseDto;
import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import org.mapstruct.Condition;
import org.mapstruct.Context;
import java.util.List;

public interface BaseMapper<E extends BaseEntity, D extends BaseDto> {

    E toEntity(D dto, @Context CycleAvoidingMappingContext context);
    D toDto(E entity, @Context CycleAvoidingMappingContext context);
    List<E> toEntity(List<D> dtoList, @Context CycleAvoidingMappingContext context);
    List<D> toDto(List<E> entityList, @Context CycleAvoidingMappingContext context);
}