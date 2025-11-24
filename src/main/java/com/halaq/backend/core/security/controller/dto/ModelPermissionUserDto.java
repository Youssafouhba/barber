package com.halaq.backend.core.security.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.halaq.backend.core.security.controller.dto.ActionPermissionDto;
import com.halaq.backend.core.security.controller.dto.ModelPermissionDto;
import com.halaq.backend.core.security.controller.dto.UserDto;
import com.halaq.backend.core.dto.AuditBaseDto;
import com.halaq.backend.core.security.controller.dto.ActionPermissionDto;
import com.halaq.backend.core.security.controller.dto.ModelPermissionDto;
import com.halaq.backend.core.security.controller.dto.UserDto;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelPermissionUserDto  extends AuditBaseDto {

    private Boolean value  ;
    private String subAttribute  ;

    private com.halaq.backend.core.security.controller.dto.ActionPermissionDto actionPermission ;
    private ModelPermissionDto modelPermission ;
    private com.halaq.backend.core.security.controller.dto.UserDto user ;



    public ModelPermissionUserDto(){
        super();
    }




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


    public com.halaq.backend.core.security.controller.dto.ActionPermissionDto getActionPermission(){
        return this.actionPermission;
    }

    public void setActionPermission(ActionPermissionDto actionPermission){
        this.actionPermission = actionPermission;
    }
    public ModelPermissionDto getModelPermission(){
        return this.modelPermission;
    }

    public void setModelPermission(ModelPermissionDto modelPermission){
        this.modelPermission = modelPermission;
    }
    public com.halaq.backend.core.security.controller.dto.UserDto getUser(){
        return this.user;
    }

    public void setUser(UserDto user){
        this.user = user;
    }






}
