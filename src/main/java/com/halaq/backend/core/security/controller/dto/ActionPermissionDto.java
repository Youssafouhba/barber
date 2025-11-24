package com.halaq.backend.core.security.controller.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;





@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionPermissionDto  extends AuditBaseDto {

    private String reference  ;
    private String libelle  ;




    public ActionPermissionDto(){
        super();
    }




    public String getReference(){
        return this.reference;
    }
    public void setReference(String reference){
        this.reference = reference;
    }


    public String getLibelle(){
        return this.libelle;
    }
    public void setLibelle(String libelle){
        this.libelle = libelle;
    }








}
