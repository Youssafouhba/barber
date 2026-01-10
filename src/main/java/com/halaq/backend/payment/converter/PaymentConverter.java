package com.halaq.backend.payment.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.payment.dto.PaymentDto;
import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.payment.mapper.PaymentMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PaymentConverter extends AbstractConverter<Payment, PaymentDto> {

    private final PaymentMapper paymentMapper;

    public PaymentConverter(PaymentMapper paymentMapper) {
        super(Payment.class, PaymentDto.class);
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment toItem(PaymentDto dto) {
        return dto == null ? null : paymentMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public PaymentDto toDto(Payment item) {
        return item == null ? null : paymentMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected PaymentDto mapToDtoInternal(Payment item, CycleAvoidingMappingContext context) {
        return paymentMapper.toDto(item, context);
    }
}