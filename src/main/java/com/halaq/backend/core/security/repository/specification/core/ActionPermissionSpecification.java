package com.halaq.backend.core.security.repository.specification.core;

import com.halaq.backend.core.security.entity.ActionPermission;
import com.halaq.backend.core.security.repository.criteria.core.ActionPermissionCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;


public class ActionPermissionSpecification extends  AbstractSpecification<ActionPermissionCriteria, ActionPermission>  {



    public ActionPermissionSpecification(ActionPermissionCriteria criteria) {
        super(criteria);
    }

    public ActionPermissionSpecification(ActionPermissionCriteria criteria, boolean distinct) {
        super(criteria, distinct);
    }

}
