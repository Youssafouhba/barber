package com.halaq.backend.payment.converter;

import com.halaq.backend.payment.dto.WalletDto;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.payment.mapper.WalletMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class WalletConverter extends AbstractConverter<Wallet, WalletDto> {

    private final WalletMapper walletMapper;

    public WalletConverter(WalletMapper walletMapper) {
        super(Wallet.class, WalletDto.class);
        this.walletMapper = walletMapper;
    }

    @Override
    public Wallet toItem(WalletDto dto) {
        return dto == null ? null : walletMapper.toEntity(dto, new com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext());
    }

    @Override
    public WalletDto toDto(Wallet item) {
        return item == null ? null : walletMapper.toDto(item, new com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext());
    }

    @Override
    protected WalletDto mapToDtoInternal(Wallet item, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context) {
        return null;
    }
}

