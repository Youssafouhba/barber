package com.halaq.backend.user.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.dto.ClientDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper extends BaseMapper<Client, ClientDto> {
}