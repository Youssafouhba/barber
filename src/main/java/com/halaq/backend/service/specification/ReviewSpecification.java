package com.halaq.backend.service.specification;

import com.halaq.backend.service.entity.Review;
import com.halaq.backend.service.criteria.ReviewCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class ReviewSpecification extends AbstractSpecification<ReviewCriteria, Review> {

    public ReviewSpecification(ReviewCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("author", "id", criteria.getAuthorId());
        addPredicateFk("barber", "id", criteria.getBarberId());
        addPredicateFk("booking", "id", criteria.getBookingId());
        addPredicate("rating", criteria.getRatingMin().toString(), Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        addPredicate("rating", criteria.getRatingMax().toString(), Operator.LESS_THAN_OR_EQUAL_TO.toString());
    }
}