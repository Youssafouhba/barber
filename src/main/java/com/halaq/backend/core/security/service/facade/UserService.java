package com.halaq.backend.core.security.service.facade;


import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.repository.criteria.core.UserCriteria;
import com.halaq.backend.core.service.IService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends IService<User, UserCriteria>, UserDetailsService {

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameWithRoles(String username);

    String cryptPassword(String value);

    boolean changePassword(String username, String newPassword);

    User findByUsernameOrEmailOrPhone(String login);

    int deleteByUsername(String username);

    UserDetails loadUserByUsername(String username);

    String generateCode(int length);

    User createAndDisable(User t);


    User findByPhone(String phone);

    User findByIdWithAllAssociations(Long id);

    User findByPhoneWithRoles(String login);

    User findByEmailWithRoles(String login);

    User updateStatusAndVerification(User user);
}
