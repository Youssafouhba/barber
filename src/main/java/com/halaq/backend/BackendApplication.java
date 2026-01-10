package com.halaq.backend;

import com.halaq.backend.core.security.common.AuthoritiesProperties;
import com.halaq.backend.core.security.config.CorsProperties;
import com.halaq.backend.core.security.config.JwtProperties;
import com.halaq.backend.core.security.config.LoginAttemptProperties;
import com.halaq.backend.core.security.config.SecurityProperties;
import com.halaq.backend.core.security.jwt.JwtClaimsConfig;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties({
        // Active la lecture des propriétés personnalisées depuis application.yml
        JwtProperties.class,
        LoginAttemptProperties.class,
        SecurityProperties.class,
        AuthoritiesProperties.class,
        CorsProperties.class,
        JwtClaimsConfig.class,
})
@EnableAsync
@EnableJpaAuditing
public class BackendApplication {
    public static ConfigurableApplicationContext ctx;


    public static ConfigurableApplicationContext getCtx() {
        return ctx;
    }

    public static void main(String[] args) {
        ctx=SpringApplication.run(BackendApplication.class, args);
    }




}
