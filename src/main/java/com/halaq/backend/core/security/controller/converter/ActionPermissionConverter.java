package com.halaq.backend.core.security.controller.converter;

import com.halaq.backend.core.converter.AbstractConverter;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.core.security.controller.dto.ActionPermissionDto;
import com.halaq.backend.core.security.entity.ActionPermission;
import com.halaq.backend.core.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
public class ActionPermissionConverter extends AbstractConverter<ActionPermission, ActionPermissionDto> {


    public  ActionPermissionConverter(){//){
        super(ActionPermission.class, ActionPermissionDto.class);
    }

    @Override
    public ActionPermission toItem(ActionPermissionDto dto) {
        if (dto == null) {
            return null;
        } else {
        ActionPermission item = new ActionPermission();
            if(StringUtil.isNotEmpty(dto.getId()))
                item.setId(dto.getId());
            if(StringUtil.isNotEmpty(dto.getReference()))
                item.setReference(dto.getReference());
            if(StringUtil.isNotEmpty(dto.getLibelle()))
                item.setLibelle(dto.getLibelle());



        return item;
        }
    }

    @Override
    public ActionPermissionDto toDto(ActionPermission item) {
        if (item == null) {
            return null;
        } else {
            ActionPermissionDto dto = new ActionPermissionDto();
            if(StringUtil.isNotEmpty(item.getId()))
                dto.setId(item.getId());
            if(StringUtil.isNotEmpty(item.getReference()))
                dto.setReference(item.getReference());
            if(StringUtil.isNotEmpty(item.getLibelle()))
                dto.setLibelle(item.getLibelle());


        return dto;
        }
    }

    @Override
    protected ActionPermissionDto mapToDtoInternal(ActionPermission item, CycleAvoidingMappingContext context) {
        return null;
    }


    public void initObject(boolean value) {
    }


}
