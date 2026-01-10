package com.halaq.backend.core.security.controller.converter;

import com.halaq.backend.core.converter.AbstractConverter;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.core.security.controller.dto.ModelPermissionDto;
import com.halaq.backend.core.security.entity.ModelPermission;
import com.halaq.backend.core.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
public class ModelPermissionConverter extends AbstractConverter<ModelPermission, ModelPermissionDto> {


    public  ModelPermissionConverter(){//){
        super(ModelPermission.class, ModelPermissionDto.class);
    }

    @Override
    public ModelPermission toItem(ModelPermissionDto dto) {
        if (dto == null) {
            return null;
        } else {
        ModelPermission item = new ModelPermission();
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
    public ModelPermissionDto toDto(ModelPermission item) {
        if (item == null) {
            return null;
        } else {
            ModelPermissionDto dto = new ModelPermissionDto();
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
    protected ModelPermissionDto mapToDtoInternal(ModelPermission item, CycleAvoidingMappingContext context) {
        return toDto(item);
    }


    public void initObject(boolean value) {
    }


}
