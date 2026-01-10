package com.halaq.backend.payment.specification;

import com.halaq.backend.payment.criteria.TransactionCriteria;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class TransactionSpecification extends AbstractSpecification<TransactionCriteria, Transaction> {

    public TransactionSpecification(TransactionCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("user", "id", criteria.getUserId() != null ? criteria.getUserId().toString() : null);
        addPredicate("type", criteria.getType() != null ? criteria.getType().toString() : null, Operator.EQUALS.toString());
        addPredicate("paymentMethod", criteria.getPaymentMethod() != null ? criteria.getPaymentMethod().toString() : null, Operator.EQUALS.toString());
        addPredicate("status", criteria.getStatus() != null ? criteria.getStatus().toString() : null, Operator.EQUALS.toString());
        
        if (criteria.getAmountMin() != null) {
            addPredicate("amount", criteria.getAmountMin().toString(), Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }
        if (criteria.getAmountMax() != null) {
            addPredicate("amount", criteria.getAmountMax().toString(), Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }
    }
}

