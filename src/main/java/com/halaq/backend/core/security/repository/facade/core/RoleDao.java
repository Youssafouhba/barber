package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.security.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleDao extends AbstractRepository<Role, Long> {

    Role findByAuthority(String authority);

    int deleteByAuthority(String authority);

    @Query("SELECT NEW Role(item.id, item.authority) FROM Role item")
    List<Role> findAllOptimized();

    // FIXED: Get Role objects, not RoleUser
    @Query("SELECT DISTINCT r FROM Role r WHERE r.authority IN :roleNames")
    List<Role> findByAuthoritiesIn(@Param("roleNames") List<String> roleNames);

    // NEW: Batch check if roles exist
    @Query("SELECT r FROM Role r WHERE r.authority IN :roleNames")
    List<Role> findByAuthoritiesInWithDetails(@Param("roleNames") List<String> roleNames);
}