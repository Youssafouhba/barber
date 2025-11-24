package com.halaq.backend.user.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.ClientDto;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.mapper.ClientMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ClientConverter extends AbstractConverter<Client, ClientDto> {

    private final ClientMapper clientMapper;

    public ClientConverter(ClientMapper clientMapper) {
        super(Client.class, ClientDto.class);
        this.clientMapper = clientMapper;
    }

    @Override
    public Client toItem(ClientDto dto) {
        return dto == null ? null : clientMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public ClientDto toDto(Client item) {
        return item == null ? null : clientMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected ClientDto mapToDtoInternal(Client item, CycleAvoidingMappingContext context) {
        return clientMapper.toDto(item, context);
    }
}
