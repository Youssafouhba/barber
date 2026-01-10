package com.halaq.backend.payment.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletCriteria extends BaseCriteria {
    private Long userId;
    private BigDecimal balanceMin;
    private BigDecimal balanceMax;
}

