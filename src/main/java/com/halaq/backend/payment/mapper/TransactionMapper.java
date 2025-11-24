package com.halaq.backend.payment.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.payment.dto.TransactionDto;
import com.halaq.backend.payment.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends BaseMapper<Transaction, TransactionDto> {

    @Override
    @Mapping(target = "userId", expression = "java(entity.getUser() != null ? entity.getUser().getId() : null)")
    TransactionDto toDto(Transaction entity, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context);

    @Override
    @Mapping(target = "user", ignore = true)
    Transaction toEntity(TransactionDto dto, com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext context);
}

