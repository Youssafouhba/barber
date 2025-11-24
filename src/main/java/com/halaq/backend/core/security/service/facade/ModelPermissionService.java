package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.entity.ModelPermission;
import com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;



public interface ModelPermissionService extends  IService<ModelPermission, ModelPermissionCriteria>  {
    List<ModelPermission> findAllOptimized();

}
