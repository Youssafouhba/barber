package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.repository.criteria.core.RoleCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;


public interface RoleService extends  IService<Role, RoleCriteria>  {
    Role findByAuthority(String authority);
    int deleteByAuthority(String authority);


    List<Role> findByAuthoritiesIn(List<String> roleNames);
}
