package com.halaq.backend.core.security.repository.criteria.core;


import com.halaq.backend.core.security.repository.criteria.core.ActionPermissionCriteria;
import com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;
import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.security.repository.criteria.core.ActionPermissionCriteria;
import com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;

import java.util.List;

public class ModelPermissionUserCriteria extends  BaseCriteria  {

    private Boolean value;
    private String subAttribute;
    private String subAttributeLike;

    private ActionPermissionCriteria actionPermission ;
    private List<ActionPermissionCriteria> actionPermissions ;
    private com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria modelPermission ;
    private List<com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria> modelPermissions ;
    private UserCriteria user ;
    private List<UserCriteria> utilisateurs ;


    public ModelPermissionUserCriteria(){}

    public Boolean getValue(){
        return this.value;
    }
    public void setValue(Boolean value){
        this.value = value;
    }
    public String getSubAttribute(){
        return this.subAttribute;
    }
    public void setSubAttribute(String subAttribute){
        this.subAttribute = subAttribute;
    }
    public String getSubAttributeLike(){
        return this.subAttributeLike;
    }
    public void setSubAttributeLike(String subAttributeLike){
        this.subAttributeLike = subAttributeLike;
    }


    public ActionPermissionCriteria getActionPermission(){
        return this.actionPermission;
    }

    public void setActionPermission(ActionPermissionCriteria actionPermission){
        this.actionPermission = actionPermission;
    }
    public List<ActionPermissionCriteria> getActionPermissions(){
        return this.actionPermissions;
    }

    public void setActionPermissions(List<ActionPermissionCriteria> actionPermissions){
        this.actionPermissions = actionPermissions;
    }
    public com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria getModelPermission(){
        return this.modelPermission;
    }

    public void setModelPermission(com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria modelPermission){
        this.modelPermission = modelPermission;
    }
    public List<com.halaq.backend.core.security.repository.criteria.core.ModelPermissionCriteria> getModelPermissions(){
        return this.modelPermissions;
    }

    public void setModelPermissions(List<ModelPermissionCriteria> modelPermissions){
        this.modelPermissions = modelPermissions;
    }
    public UserCriteria getUser(){
        return this.user;
    }

    public void setUser(UserCriteria user){
        this.user = user;
    }
    public List<UserCriteria> getUsers(){
        return this.utilisateurs;
    }

    public void setUsers(List<UserCriteria> utilisateurs){
        this.utilisateurs = utilisateurs;
    }
}
