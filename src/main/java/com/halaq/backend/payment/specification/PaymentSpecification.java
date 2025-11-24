package com.halaq.backend.payment.specification;

import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.payment.criteria.PaymentCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class PaymentSpecification extends AbstractSpecification<PaymentCriteria, Payment> {

    public PaymentSpecification(PaymentCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("booking", "id", criteria.getBookingId());
        addPredicate("status", criteria.getStatus().toString(), Operator.EQUALS.toString());
        addPredicate("method", criteria.getMethod().toString(), Operator.EQUALS.toString());
    }
}