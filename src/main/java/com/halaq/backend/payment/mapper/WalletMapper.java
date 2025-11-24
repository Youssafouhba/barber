package com.halaq.backend.payment.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.payment.dto.WalletDto;
import com.halaq.backend.payment.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper extends BaseMapper<Wallet, WalletDto> {

    @Override
    @Mapping(target = "userId", expression = "java(entity.getUser() != null ? entity.getUser().getId() : null)")
    WalletDto toDto(Wallet entity, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context);

    @Override
    @Mapping(target = "user", ignore = true)
    Wallet toEntity(WalletDto dto, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context);
}

