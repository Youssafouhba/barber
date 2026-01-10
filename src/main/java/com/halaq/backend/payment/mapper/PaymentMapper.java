package com.halaq.backend.payment.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.payment.dto.PaymentDto;
import com.halaq.backend.payment.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper extends BaseMapper<Payment, PaymentDto> {

}