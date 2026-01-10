package com.halaq.backend.service.specification;

import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;
import com.halaq.backend.service.criteria.TimeBlockCriteria;
import com.halaq.backend.service.entity.TimeBlock;

public class TimeBlockSpecification extends AbstractSpecification<TimeBlockCriteria, TimeBlock> {

    public TimeBlockSpecification(TimeBlockCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("barber", "id", criteria.getBarberId());

        addPredicate("startDateTime", criteria.getStartFrom() != null ? criteria.getStartFrom().toString() : null, Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        addPredicate("startDateTime", criteria.getStartTo() != null ? criteria.getStartTo().toString() : null, Operator.LESS_THAN_OR_EQUAL_TO.toString());

        addPredicate("endDateTime", criteria.getEndFrom() != null ? criteria.getEndFrom().toString() : null, Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        addPredicate("endDateTime", criteria.getEndTo() != null ? criteria.getEndTo().toString() : null, Operator.LESS_THAN_OR_EQUAL_TO.toString());
    }
}


