package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.entity.ActionPermission;
import com.halaq.backend.core.security.repository.criteria.core.ActionPermissionCriteria;
import com.halaq.backend.core.security.repository.facade.core.ActionPermissionDao;
import com.halaq.backend.core.security.repository.specification.core.ActionPermissionSpecification;
import com.halaq.backend.core.security.service.facade.ActionPermissionService;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActionPermissionServiceImpl extends AbstractServiceImpl<ActionPermission, ActionPermissionCriteria, ActionPermissionDao> implements ActionPermissionService {





    public ActionPermission findByReferenceEntity(ActionPermission t){
        return  dao.findByReference(t.getReference());
    }


    public List<ActionPermission> findAllOptimized() {
        return dao.findAllOptimized();
    }





    public void configure() {
        super.configure(ActionPermission.class, ActionPermissionSpecification.class);
    }



    public ActionPermissionServiceImpl(ActionPermissionDao dao) {
        super(dao);
    }

}
