package com.halaq.backend.core.security.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.halaq.backend.core.dto.AuditBaseDto;
import com.halaq.backend.core.security.controller.dto.RoleDto;
import com.halaq.backend.core.security.controller.dto.UserDto;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleUserDto  extends AuditBaseDto {


    private RoleDto role ;
    private com.halaq.backend.core.security.controller.dto.UserDto user ;



    public RoleUserDto(){
        super();
    }




    public RoleDto getRole(){
        return this.role;
    }

    public void setRole(RoleDto role){
        this.role = role;
    }
    public com.halaq.backend.core.security.controller.dto.UserDto getUser(){
        return this.user;
    }

    public void setUser(UserDto user){
        this.user = user;
    }






}
