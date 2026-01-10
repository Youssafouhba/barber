package com.halaq.backend.service.specification;

import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.criteria.ServiceCategoryCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class ServiceCategorySpecification extends AbstractSpecification<ServiceCategoryCriteria, ServiceCategory> {

    public ServiceCategorySpecification(ServiceCategoryCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicate("name", criteria.getName(), Operator.LIKE_LOWER.toString());
    }
}