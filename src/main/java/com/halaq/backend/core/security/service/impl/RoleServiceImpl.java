package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.repository.criteria.core.RoleCriteria;
import com.halaq.backend.core.security.repository.facade.core.RoleDao;
import com.halaq.backend.core.security.repository.specification.core.RoleSpecification;
import com.halaq.backend.core.security.service.facade.RoleService;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends AbstractServiceImpl<Role, RoleCriteria, RoleDao> implements RoleService {


    @Override
    public Role findByAuthority(String authority) {
        return dao.findByAuthority(authority);
    }

    @Override
    public int deleteByAuthority(String authority) {
        return deleteByAuthority(authority);
    }

    @Override
    public List<Role> findByAuthoritiesIn(List<String> roleNames) {
        return dao.findByAuthoritiesIn(roleNames);
    }


    public Role findByReferenceEntity(Role t){
        return  dao.findByAuthority(t.getAuthority());
    }


    public List<Role> findAllOptimized() {
        return dao.findAllOptimized();
    }





    public void configure() {
        super.configure(Role.class, RoleSpecification.class);
    }



    public RoleServiceImpl(RoleDao dao) {
        super(dao);
    }

}
