package com.halaq.backend.user.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.DocumentDto;
import com.halaq.backend.user.entity.Document;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper extends BaseMapper<Document, DocumentDto> {
    @Mapping(target = "barber", ignore = true)
    @Override
    DocumentDto toDto(Document entity, @Context CycleAvoidingMappingContext context);
}