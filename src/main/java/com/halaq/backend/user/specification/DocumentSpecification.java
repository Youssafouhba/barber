package com.halaq.backend.user.specification;

import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.criteria.DocumentCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class DocumentSpecification extends AbstractSpecification<DocumentCriteria, Document> {

    public DocumentSpecification(DocumentCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("barber", "id", criteria.getBarberId());
        addPredicate("type", criteria.getType().toString(), Operator.EQUALS.toString());
        addPredicate("verificationStatus", criteria.getVerificationStatus().toString(), Operator.EQUALS.toString());
    }
}