package com.halaq.backend.user.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.DocumentDto;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.mapper.DocumentMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class DocumentConverter extends AbstractConverter<Document, DocumentDto> {

    private final DocumentMapper documentMapper;

    public DocumentConverter(DocumentMapper documentMapper) {
        super(Document.class, DocumentDto.class);
        this.documentMapper = documentMapper;
    }

    @Override
    public Document toItem(DocumentDto dto) {
        return dto == null ? null : documentMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public DocumentDto toDto(Document item) {
        return item == null ? null : documentMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected DocumentDto mapToDtoInternal(Document item, CycleAvoidingMappingContext context) {
        return documentMapper.toDto(item, context);
    }
}