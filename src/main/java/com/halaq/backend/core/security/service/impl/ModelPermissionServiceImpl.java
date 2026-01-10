package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.entity.ModelPermission;
import com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria;
import com.halaq.backend.core.security.repository.facade.core.ModelPermissionDao;
import com.halaq.backend.core.security.repository.specification.core.ModelPermissionSpecification;
import com.halaq.backend.core.security.service.facade.ModelPermissionService;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelPermissionServiceImpl extends AbstractServiceImpl<ModelPermission, ModelPermissionCriteria, ModelPermissionDao> implements ModelPermissionService {



    public ModelPermission findByReferenceEntity(ModelPermission t){
        return  dao.findByReference(t.getReference());
    }


    public List<ModelPermission> findAllOptimized() {
        return dao.findAllOptimized();
    }





    public void configure() {
        super.configure(ModelPermission.class, ModelPermissionSpecification.class);
    }



    public ModelPermissionServiceImpl(ModelPermissionDao dao) {
        super(dao);
    }

}
