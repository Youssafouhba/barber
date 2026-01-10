package com.halaq.backend.user.specification;

import com.halaq.backend.user.criteria.ClientCriteria;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;

public class ClientSpecification extends AbstractSpecification<ClientCriteria, Client> {

    public ClientSpecification(ClientCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        // Ajoute des prédicats basés sur les critères fournis
        addPredicate("fullName", criteria.getFullName(), Operator.LIKE.toString());
        addPredicate("email", criteria.getEmail(), Operator.LIKE.toString());
        addPredicate("phone", criteria.getPhone(), Operator.LIKE.toString());
        addPredicate("address", criteria.getAddress(), Operator.LIKE.toString());
    }
}