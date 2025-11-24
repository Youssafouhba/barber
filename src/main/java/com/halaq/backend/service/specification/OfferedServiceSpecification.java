package com.halaq.backend.service.specification;

import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.service.criteria.OfferedServiceCriteria;
import com.halaq.backend.service.entity.OfferedService;

public class OfferedServiceSpecification extends AbstractSpecification<OfferedServiceCriteria, OfferedService> {

    public OfferedServiceSpecification(OfferedServiceCriteria criteria) {
        super(criteria);
    }

}