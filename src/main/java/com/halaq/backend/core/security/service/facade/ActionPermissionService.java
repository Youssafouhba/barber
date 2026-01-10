package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.entity.ActionPermission;
import com.halaq.backend.core.security.repository.criteria.core.ActionPermissionCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;


public interface ActionPermissionService extends  IService<ActionPermission, ActionPermissionCriteria>  {

    List<ActionPermission> findAllOptimized();

}
