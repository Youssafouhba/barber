package com.halaq.backend.service.specification;

import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.criteria.AvailabilityCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class AvailabilitySpecification extends AbstractSpecification<AvailabilityCriteria, Availability> {

    public AvailabilitySpecification(AvailabilityCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("barber", "id", criteria.getBarberId());
        addPredicate("day", criteria.getDay().toString(), Operator.EQUALS.toString());
    }
}