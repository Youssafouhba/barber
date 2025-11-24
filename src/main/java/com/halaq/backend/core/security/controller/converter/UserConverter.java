package com.halaq.backend.core.security.controller.converter;

import com.halaq.backend.core.converter.AbstractConverter;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.core.security.controller.converter.ActionPermissionConverter;
import com.halaq.backend.core.security.controller.converter.ModelPermissionUserConverter;
import com.halaq.backend.core.security.controller.converter.RoleConverter;
import com.halaq.backend.core.security.controller.converter.RoleUserConverter;
import com.halaq.backend.core.security.controller.dto.UserDto;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.util.DateUtil;
import com.halaq.backend.core.util.ListUtil;
import com.halaq.backend.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserConverter extends AbstractConverter<User, UserDto> {

    @Autowired
    private RoleUserConverter roleUserConverter ;
    @Autowired
    private RoleConverter roleConverter ;
    @Autowired
    private ModelPermissionUserConverter modelPermissionUserConverter ;
    @Autowired
    private com.halaq.backend.core.security.controller.converter.ActionPermissionConverter actionPermissionConverter ;
    private boolean modelPermissionUsers;
    private boolean roleUsers;

    public  UserConverter(){//RoleUser roleUserModelPermission modelPermissionRole roleModelPermissionUser modelPermissionUserActionPermission actionPermission,){
        super(User.class, UserDto.class);
        init(true);
        //this.roleUser =  roleUser ;
        //this.modelPermission =  modelPermission ;
        //this.role =  role ;
        //this.modelPermissionUser =  modelPermissionUser ;
        //this.actionPermission =  actionPermission ;
    }

    public User toItem(UserDto dto) {
        if (dto == null) {
            return null;
        } else {
        User item = new User();
            if(StringUtil.isNotEmpty(dto.getId()))
                item.setId(dto.getId());
            if(dto.getCredentialsNonExpired() != null)
                item.setCredentialsNonExpired(dto.getCredentialsNonExpired());
            if(dto.getEnabled() != null)
                item.setEnabled(dto.getEnabled());
            if(StringUtil.isNotEmpty(dto.getEmail()))
                item.setEmail(dto.getEmail());
            if(dto.getAccountNonExpired() != null)
                item.setAccountNonExpired(dto.getAccountNonExpired());
            if(dto.getAccountNonLocked() != null)
                item.setAccountNonLocked(dto.getAccountNonLocked());
            if(StringUtil.isNotEmpty(dto.getUsername()))
                item.setUsername(dto.getUsername());
            if(StringUtil.isNotEmpty(dto.getPassword()))
                item.setPassword(dto.getPassword());
            if(dto.getPasswordChanged() != null)
                item.setPasswordChanged(dto.getPasswordChanged());

            if(StringUtil.isNotEmpty(dto.getFirstName()))
                item.setFirstName(dto.getFirstName());

            if(StringUtil.isNotEmpty(dto.getLastName()))
                item.setLastName(dto.getLastName());

            if(StringUtil.isNotEmpty(dto.getPhone()))
                item.setPhone(dto.getPhone());

            if(StringUtil.isNotEmpty(dto.getLinkValidationCode()))
                item.setLinkValidationCode(dto.getLinkValidationCode());

            if (dto.getExpirationLinkDate() != null)
                item.setExpirationLinkDate(dto.getExpirationLinkDate());



            if(this.modelPermissionUsers && ListUtil.isNotEmpty(dto.getModelPermissionUsers()))
                item.setModelPermissionUsers(modelPermissionUserConverter.toItem(dto.getModelPermissionUsers()));
            if(this.roleUsers && ListUtil.isNotEmpty(dto.getRoleUsers()))
                item.setRoleUsers(roleUserConverter.toItem(dto.getRoleUsers()));

        return item;
        }
    }

    @Override
    public UserDto toDto(User item) {
        if (item == null) {
            return null;
        } else {
            UserDto dto = new UserDto();
            if(StringUtil.isNotEmpty(item.getId()))
                dto.setId(item.getId());
            if(StringUtil.isNotEmpty(item.getFirstName()))
                dto.setFirstName(item.getFirstName());
            if(StringUtil.isNotEmpty(item.getLastName()))
                dto.setLastName(item.getLastName());
            if(StringUtil.isNotEmpty(item.getPhone()))
                dto.setPhone(item.getPhone());
            if(StringUtil.isNotEmpty(item.isCredentialsNonExpired()))
                dto.setCredentialsNonExpired(item.isCredentialsNonExpired());
            if(StringUtil.isNotEmpty(item.isEnabled()))
                dto.setEnabled(item.isEnabled());
            if((item.getCreatedAt()!= null))
                dto.setCreatedOn(DateUtil.dateToString(item.getCreatedAt()));
            if((item.getUpdatedAt()!= null))
                dto.setUpdatedOn(DateUtil.dateToString(item.getUpdatedAt()));
            if(StringUtil.isNotEmpty(item.getEmail()))
                dto.setEmail(item.getEmail());
            if(StringUtil.isNotEmpty(item.isAccountNonExpired()))
                dto.setAccountNonExpired(item.isAccountNonExpired());
            if(StringUtil.isNotEmpty(item.isAccountNonLocked()))
                dto.setAccountNonLocked(item.isAccountNonLocked());
            if(StringUtil.isNotEmpty(item.getUsername()))
                dto.setUsername(item.getUsername());
            if(StringUtil.isNotEmpty(item.getPassword()))
                dto.setPassword(item.getPassword());
            if(StringUtil.isNotEmpty(item.getPasswordChanged()))
                dto.setPasswordChanged(item.getPasswordChanged());
        if(this.modelPermissionUsers && ListUtil.isNotEmpty(item.getModelPermissionUsers())){
            modelPermissionUserConverter.init(true);
            modelPermissionUserConverter.setUser(false);
            dto.setModelPermissionUsers(modelPermissionUserConverter.toDto(item.getModelPermissionUsers()));
            modelPermissionUserConverter.setUser(true);

        }
        if(this.roleUsers && ListUtil.isNotEmpty(item.getRoleUsers())){
            roleUserConverter.init(true);
            roleUserConverter.setUser(false);
            dto.setRoleUsers(roleUserConverter.toDto(item.getRoleUsers()));
            roleUserConverter.setUser(true);
        }
        return dto;
        }
    }

    @Override
    protected UserDto mapToDtoInternal(User item, CycleAvoidingMappingContext context) {
        return null;
    }

    public void initList(boolean value) {
        this.modelPermissionUsers = value;
        this.roleUsers = value;
    }

    public void initObject(boolean value) {
    }


    public RoleUserConverter getRoleUserConverter(){
        return this.roleUserConverter;
    }
    public void setRoleUserConverter(RoleUserConverter roleUserConverter ){
        this.roleUserConverter = roleUserConverter;
    }

    public RoleConverter getRoleConverter(){
        return this.roleConverter;
    }
    public void setRoleConverter(RoleConverter roleConverter ){
        this.roleConverter = roleConverter;
    }
    public ModelPermissionUserConverter getModelPermissionUserConverter(){
        return this.modelPermissionUserConverter;
    }
    public void setModelPermissionUserConverter(ModelPermissionUserConverter modelPermissionUserConverter ){
        this.modelPermissionUserConverter = modelPermissionUserConverter;
    }
    public com.halaq.backend.core.security.controller.converter.ActionPermissionConverter getActionPermissionConverter(){
        return this.actionPermissionConverter;
    }
    public void setActionPermissionConverter(ActionPermissionConverter actionPermissionConverter ){
        this.actionPermissionConverter = actionPermissionConverter;
    }
    public boolean  isModelPermissionUsers(){
        return this.modelPermissionUsers ;
    }
    public void  setModelPermissionUsers(boolean modelPermissionUsers ){
        this.modelPermissionUsers  = modelPermissionUsers ;
    }
    public boolean  isRoleUsers(){
        return this.roleUsers ;
    }
    public void  setRoleUsers(boolean roleUsers ){
        this.roleUsers  = roleUsers ;
    }
}
