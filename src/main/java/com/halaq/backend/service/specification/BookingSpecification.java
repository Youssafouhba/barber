package com.halaq.backend.service.specification;

import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class BookingSpecification extends AbstractSpecification<BookingCriteria, Booking> {

    public BookingSpecification(BookingCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicate("status", criteria.getStatus().toString(), Operator.EQUALS.toString());

        // Exemple de filtre sur des clés étrangères (Foreign Keys)
        addPredicateFk("client", "id", criteria.getClientId());
        addPredicateFk("barber", "id", criteria.getBarberId());

        // Exemple de filtre sur un intervalle de dates
        addPredicate("scheduledAt", criteria.getScheduledAtFrom().toString(), Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        addPredicate("scheduledAt", criteria.getScheduledAtTo().toString(), Operator.LESS_THAN_OR_EQUAL_TO.toString());
    }
}