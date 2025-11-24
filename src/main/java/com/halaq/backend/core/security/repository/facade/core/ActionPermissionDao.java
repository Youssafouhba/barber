package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.security.entity.ActionPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ActionPermissionDao extends AbstractRepository<ActionPermission,Long>  {
    ActionPermission findByReference(String reference);
    int deleteByReference(String reference);


    @Query("SELECT NEW ActionPermission(item.id,item.reference) FROM ActionPermission item")
    List<ActionPermission> findAllOptimized();

}
