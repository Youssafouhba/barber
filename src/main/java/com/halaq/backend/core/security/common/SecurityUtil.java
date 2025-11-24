package com.halaq.backend.core.security.common;

import com.halaq.backend.BackendApplication;
import com.halaq.backend.core.security.common.AuthoritiesProperties;
import com.halaq.backend.core.security.common.AuthoritiesProperties;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.service.facade.BarberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;


@Service
public class SecurityUtil {


    public static User getCurrentUser() {
        UserService userService= BackendApplication.getCtx().getBean(UserService.class);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Object user = securityContext.getAuthentication().getPrincipal();
        if (user instanceof String) {
            return userService.findByUsername((String) user);
        } else if (user instanceof User) {
            return (User) user;
        } else {
            return null;
        }
    }
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                getAuthorities(authentication).noneMatch(AuthoritiesProperties.ANONYMOUS::equals);
    }


    public static boolean isCurrentUserInRole(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                getAuthorities(authentication).anyMatch(authority::equals);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority);
    }

}
