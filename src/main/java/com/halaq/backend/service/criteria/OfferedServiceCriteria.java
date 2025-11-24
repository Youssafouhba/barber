package com.halaq.backend.service.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OfferedServiceCriteria extends BaseCriteria {
    private String name;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Long categoryId;
}