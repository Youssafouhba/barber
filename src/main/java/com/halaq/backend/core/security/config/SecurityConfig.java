package com.halaq.backend.core.security.config;


import com.halaq.backend.core.security.jwt.AuthEntryPointJwt;
import com.halaq.backend.core.security.jwt.AuthTokenFilter;
import com.halaq.backend.core.security.service.facade.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final SecurityProperties securityProperties;
    @Autowired
    private  UserService userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    public SecurityConfig(CorsProperties corsProperties, SecurityProperties securityProperties, AuthEntryPointJwt unauthorizedHandler) {
        this.corsProperties = corsProperties;
        this.securityProperties = securityProperties;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(corsProperties.getAllowedOrigins()));
        configuration.setAllowedMethods(List.of(corsProperties.getAllowedMethods()));
        configuration.setAllowedHeaders(List.of(corsProperties.getAllowedHeaders()));
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityContext((securityContext) -> securityContext.requireExplicitSave(false));
        // 1. Appliquer la configuration CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 2. Désactiver CSRF car nous utilisons des tokens JWT (stateless)
        http.csrf(AbstractHttpConfigurer::disable);
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
        // 3. Définir la politique de session sur STATELESS
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. Définir les règles d'autorisation pour les endpoints
        http.authorizeHttpRequests(auth -> {
            // Appliquer les règles "permitAll" depuis le fichier YAML
            auth.requestMatchers(securityProperties.getPermitAll().toArray(new String[0])).permitAll();

            // Appliquer les règles par rôle depuis le fichier YAML
            for (SecurityProperties.RoleMapping roleMapping : securityProperties.getRoles()) {
                auth.requestMatchers(roleMapping.getPaths().toArray(new String[0]))
                        .hasAnyAuthority(roleMapping.getAuthority()); // Utiliser hasAnyAuthority pour plus de flexibilité
            }
            auth.requestMatchers("/ws/**", "/updates/**", "/notifications/**").permitAll();
            auth.requestMatchers("/topic/**", "/queue/**", "/user/**").permitAll();

            // Toutes les autres requêtes doivent être authentifiées
            auth.anyRequest().authenticated();

        });
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }
}