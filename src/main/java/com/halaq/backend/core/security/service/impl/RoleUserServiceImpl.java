package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.repository.criteria.core.RoleUserCriteria;
import com.halaq.backend.core.security.repository.facade.core.RoleUserDao;
import com.halaq.backend.core.security.repository.specification.core.RoleUserSpecification;
import com.halaq.backend.core.security.service.facade.RoleService;
import com.halaq.backend.core.security.service.facade.RoleUserService;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleUserServiceImpl extends AbstractServiceImpl<RoleUser, RoleUserCriteria, RoleUserDao> implements RoleUserService {






    public List<RoleUser> findByRoleId(Long id){
        return dao.findByRoleId(id);
    }
    public int deleteByRoleId(Long id){
        return dao.deleteByRoleId(id);
    }
    public long countByRoleAuthority(String authority){
        return dao.countByRoleAuthority(authority);
    }
    public List<RoleUser> findByUserId(Long id){
        return dao.findByUserAppId(id);
    }
    public int deleteByUserId(Long id){
        return dao.deleteByUserAppId(id);
    }
    public long countByUserEmail(String email){
        return dao.countByUserAppEmail(email);
    }

    @Override
    @Transactional // S'assurer que ceci fait partie de la transaction du parent
    public List<RoleUser> saveAll(List<RoleUser> roleUsers) {
        return dao.saveAll(roleUsers);
    }

    public void configure() {
        super.configure(RoleUser.class, RoleUserSpecification.class);
    }


    @Autowired
    private RoleService roleService ;
    public RoleUserServiceImpl(RoleUserDao dao) {
        super(dao);
    }

}
