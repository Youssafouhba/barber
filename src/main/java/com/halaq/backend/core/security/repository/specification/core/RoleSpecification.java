package com.halaq.backend.core.security.repository.specification.core;

import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.repository.criteria.core.RoleCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;


public class RoleSpecification extends  AbstractSpecification<RoleCriteria, Role>  {

    @Override
    public void constructPredicates() {
        addPredicateId("id", criteria);
        addPredicate("authority", criteria.getAuthority(),criteria.getAuthorityLike());
    }

    public RoleSpecification(RoleCriteria criteria) {
        super(criteria);
    }

    public RoleSpecification(RoleCriteria criteria, boolean distinct) {
        super(criteria, distinct);
    }

}
