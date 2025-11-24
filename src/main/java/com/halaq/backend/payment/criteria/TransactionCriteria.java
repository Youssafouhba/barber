package com.halaq.backend.payment.criteria;

import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.shared.TransactionType;
import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionCriteria extends BaseCriteria {
    private Long userId;
    private TransactionType type;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private BigDecimal amountMin;
    private BigDecimal amountMax;
}

