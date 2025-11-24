package com.halaq.backend.core.security.repository.criteria.core;


import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.security.repository.criteria.core.RoleCriteria;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;

import java.util.List;

public class RoleUserCriteria extends  BaseCriteria  {


    private com.halaq.backend.core.security.repository.criteria.core.RoleCriteria role ;
    private List<com.halaq.backend.core.security.repository.criteria.core.RoleCriteria> roles ;
    private com.halaq.backend.core.security.repository.criteria.core.UserCriteria user ;
    private List<com.halaq.backend.core.security.repository.criteria.core.UserCriteria> utilisateurs ;


    public RoleUserCriteria(){}


    public com.halaq.backend.core.security.repository.criteria.core.RoleCriteria getRole(){
        return this.role;
    }

    public void setRole(com.halaq.backend.core.security.repository.criteria.core.RoleCriteria role){
        this.role = role;
    }
    public List<com.halaq.backend.core.security.repository.criteria.core.RoleCriteria> getRoles(){
        return this.roles;
    }

    public void setRoles(List<RoleCriteria> roles){
        this.roles = roles;
    }
    public com.halaq.backend.core.security.repository.criteria.core.UserCriteria getUser(){
        return this.user;
    }

    public void setUser(com.halaq.backend.core.security.repository.criteria.core.UserCriteria user){
        this.user = user;
    }
    public List<com.halaq.backend.core.security.repository.criteria.core.UserCriteria> getUsers(){
        return this.utilisateurs;
    }

    public void setUsers(List<UserCriteria> utilisateurs){
        this.utilisateurs = utilisateurs;
    }
}
