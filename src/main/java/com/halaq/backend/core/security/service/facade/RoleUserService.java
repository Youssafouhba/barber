package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.repository.criteria.core.RoleUserCriteria;
import com.halaq.backend.core.service.IService;

import java.util.List;



public interface RoleUserService extends  IService<RoleUser, RoleUserCriteria>  {

    List<RoleUser> findByRoleId(Long id);
    int deleteByRoleId(Long id);
    long countByRoleAuthority(String authority);
    List<RoleUser> findByUserId(Long id);
    int deleteByUserId(Long id);
    long countByUserEmail(String email);
    List<RoleUser> saveAll(List<RoleUser> roleUsers);
}
