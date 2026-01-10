package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.security.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends AbstractRepository<User, Long> {

    User findByEmail(String email);

    User findByPhone(String phone);

    User findByUsername(String username);

    // OPTIMIZED: Single query with all associations (EAGER loading)
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roleUsers ru " +
            "LEFT JOIN FETCH ru.role " +
            "WHERE u.id = :id")
    User findByIdWithAllAssociations(@Param("id") Long id);

    // OPTIMIZED: Load user with roles for login
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roleUsers ru " +
            "LEFT JOIN FETCH ru.role " +
            "WHERE u.username = :username")
    User findByUsernameWithRoles(@Param("username") String username);

    // OPTIMIZED: Load user with roles by email
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roleUsers ru " +
            "LEFT JOIN FETCH ru.role " +
            "WHERE u.email = :email")
    User findByEmailWithRoles(@Param("email") String email);

    // OPTIMIZED: Load user with roles by phone
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roleUsers ru " +
            "LEFT JOIN FETCH ru.role " +
            "WHERE u.phone = :phone")
    User findByPhoneWithRoles(@Param("phone") String phone);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roleUsers ru " +
            "LEFT JOIN FETCH ru.role r " +
            "LEFT JOIN FETCH u.verificationTracker vt " +
            "WHERE u.username = :login OR u.email = :login OR u.phone = :login")
    User findByUsernameOrEmailOrPhone(String login);

    boolean existsByUsernameOrEmail(String username, String email);

    int deleteByUsername(String username);
}
