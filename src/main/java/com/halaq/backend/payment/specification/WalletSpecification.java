package com.halaq.backend.payment.specification;

import com.halaq.backend.payment.criteria.WalletCriteria;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class WalletSpecification extends AbstractSpecification<WalletCriteria, Wallet> {

    public WalletSpecification(WalletCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("user", "id", criteria.getUserId() != null ? criteria.getUserId().toString() : null);
        
        if (criteria.getBalanceMin() != null) {
            addPredicate("balance", criteria.getBalanceMin().toString(), Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }
        if (criteria.getBalanceMax() != null) {
            addPredicate("balance", criteria.getBalanceMax().toString(), Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }
    }
}

