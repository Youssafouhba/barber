package com.halaq.backend.core.security.repository.specification.core;

import com.halaq.backend.core.security.entity.ModelPermission;
import com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;


public class ModelPermissionSpecification extends  AbstractSpecification<ModelPermissionCriteria, ModelPermission>  {

    @Override
    public void constructPredicates() {
        addPredicateId("id", criteria);
        addPredicate("reference", criteria.getReference(),criteria.getReferenceLike());
        addPredicate("libelle", criteria.getLibelle(),criteria.getLibelleLike());
    }

    public ModelPermissionSpecification(ModelPermissionCriteria criteria) {
        super(criteria);
    }

    public ModelPermissionSpecification(ModelPermissionCriteria criteria, boolean distinct) {
        super(criteria, distinct);
    }

}
