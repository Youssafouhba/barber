package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.security.entity.ModelPermissionUser;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ModelPermissionUserDao extends AbstractRepository<ModelPermissionUser,Long>  {

    List<ModelPermissionUser> findByActionPermissionId(Long id);
    int deleteByActionPermissionId(Long id);
    long countByActionPermissionReference(String reference);
    List<ModelPermissionUser> findByModelPermissionId(Long id);
    int deleteByModelPermissionId(Long id);
    long countByModelPermissionReference(String reference);
    List<ModelPermissionUser> findByUserAppId(Long id);
    ModelPermissionUser findByUserAppUsernameAndModelPermissionReferenceAndActionPermissionReference( String username ,  String modelReference,  String actionReference);
    int deleteByUserAppId(Long id);
    long countByUserAppEmail(String email);
    List<ModelPermissionUser> findByUserAppUsername(String username);



}
