package com.halaq.backend.core.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Utilisé pour sérialiser la réponse d'erreur en JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            // Tente de valider le token et d'authentifier l'utilisateur
            if (jwt != null && jwtTokenUtil.validateToken(jwt)) {

                String username = jwtTokenUtil.getUsernameFromToken(jwt);
                List<String> roles = jwtTokenUtil.getRolesFromToken(jwt);

                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            // 🎯 GESTION SPÉCIFIQUE DE L'EXPIRATION DU TOKEN
            logger.warn("JWT token has expired: {}", ex.getMessage());

            // 1. Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();

            // 2. Configurer la réponse HTTP 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            // 3. Créer le corps de la réponse JSON avec un code d'erreur spécifique
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "The access token has expired. Please refresh your token.");
            errorDetails.put("exceptionType", "ExpiredJwtException");

            // 4. Écrire la réponse JSON
            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));

            // 5. Arrêter la chaîne de filtres (ne pas appeler filterChain.doFilter)
            return;

        } catch (Exception e) {
            // Gestion des autres erreurs JWT (Malformed, Unsupported, etc.)
            logger.error("Error processing JWT: {}", e.getMessage());
            // On peut choisir d'arrêter ou de laisser passer ici. Laisser passer est commun si on ne veut pas bloquer les requêtes anonymes ou publiques non censées avoir un JWT.
            SecurityContextHolder.clearContext();
        }

        // On continue la chaîne des filtres (si l'exception ExpiredJwtException n'a pas été levée)
        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT du header "Authorization".
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
