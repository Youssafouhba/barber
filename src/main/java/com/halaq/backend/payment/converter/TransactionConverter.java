package com.halaq.backend.payment.converter;

import com.halaq.backend.payment.dto.TransactionDto;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.payment.mapper.TransactionMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter extends AbstractConverter<Transaction, TransactionDto> {

    private final TransactionMapper transactionMapper;

    public TransactionConverter(TransactionMapper transactionMapper) {
        super(Transaction.class, TransactionDto.class);
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Transaction toItem(TransactionDto dto) {
        return dto == null ? null : transactionMapper.toEntity(dto, new com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext());
    }

    @Override
    public TransactionDto toDto(Transaction item) {
        return item == null ? null : transactionMapper.toDto(item, new com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext());
    }

    @Override
    protected TransactionDto mapToDtoInternal(Transaction item, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context) {
        return transactionMapper.toDto(item, context);
    }
}

