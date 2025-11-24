package com.halaq.backend.core.security.jwt;


import com.halaq.backend.core.security.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${app.security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    @Autowired
    private JwtClaimsConfig jwtClaimsConfig;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        for (String claimName : jwtClaimsConfig.getEnabledClaims()) {
            if ("roles".equalsIgnoreCase(claimName)) {
                if (user.getRoleUsers() != null && !user.getRoleUsers().isEmpty()) {
                    claims.put("roles", user.getRoleUsers().stream()
                            .map(roleUser -> roleUser.getRole().getAuthority())
                            .collect(Collectors.toList()));
                }else {
                    logger.warn("User {} authenticated but has no authorities loaded.", user.getUsername());
                }
                continue; // On passe au claim suivant
            }
            // --- Logique de Réflexion pour tous les autres claims ---
            try {
                // Construit le nom du getter correspondant au claim
                // "firstName" -> "getFirstName"
                // "enabled"   -> "isEnabled" (gère le cas des booléens)
                String getterName = (claimName.equals("enabled") ? "is" : "get")
                        + claimName.substring(0, 1).toUpperCase()
                        + claimName.substring(1);
                // Récupère la méthode de la classe User par son nom
                Method getter = user.getClass().getMethod(getterName);
                // Appelle la méthode (ex: user.getFirstName()) et récupère la valeur
                Object value = getter.invoke(user);
                // Ajoute le claim et sa valeur au token
                claims.put(claimName, value);
            } catch (NoSuchMethodException e) {
                logger.warn("Claim '{}' configuré dans application.yml, mais la méthode '{}()' correspondante n'a pas été trouvée dans la classe User.", claimName, e.getMessage());
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération du claim '{}' par réflexion.", claimName, e);
            }
        }
        return createToken(claims, user.getUsername());
    }


    public String generateTokenFromAuthentication(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return generateToken(user);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("id", Long.class);
    }

    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (List<String>) claims.get("roles");
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            // Si on ne peut pas obtenir la date d'expiration (token mal formé, etc.), on considère qu'il est expiré.
            return true;
        }
    }

    public Boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(user.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) throws ExpiredJwtException {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
            // 🎯 Relance l'exception ExpiredJwtException pour qu'elle soit gérée par le filtre
            // de sécurité ou le contrôleur pour un refresh de token.
            throw ex;
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        } catch (JwtException ex) {
            logger.error("JWT token validation error: {}", ex.getMessage());
        }
        return false;
    }

    public long getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

    // Méthodes utilitaires pour les refresh tokens
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (jwtExpirationInMs * 30)); // 30x plus long

        // On inclut les rôles dans le refresh token également !
        Map<String, Object> claims = new HashMap<>();
        if (user.getAuthorities() != null) {
            claims.put("roles", user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .setClaims(claims) // Ajoute les claims
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean canTokenBeRefreshed(String token) {
        // Un refresh token n'a pas besoin d'être validé contre son expiration ici.
        // On vérifie juste qu'il n'est pas malformé.
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Si le Refresh Token est expiré, on ne peut pas le rafraîchir.
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs));

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (ExpiredJwtException e) {
            // Si on essaie de rafraîchir un token qui vient d'expirer, c'est normal.
            // On récupère les claims et on continue le processus.
            final Claims claims = e.getClaims();
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs));

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (JwtException e) {
            logger.error("Error refreshing JWT token: {}", e.getMessage());
            throw new RuntimeException("Cannot refresh token", e);
        }
    }

    // Extraire des informations spécifiques du token pour l'audit
    public Map<String, Object> getTokenClaims(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Map<String, Object> tokenInfo = new HashMap<>();

            // On boucle sur les claims définis dans la config d'audit
            for (String claimName : jwtClaimsConfig.getAuditClaims()) {
                Object value = null;

                // On gère les claims standards du JWT qui ont des getters spécifiques
                switch (claimName) {
                    case "username":
                        value = claims.getSubject();
                        break;
                    case "issuedAt":
                        value = claims.getIssuedAt();
                        break;
                    case "expiration":
                        value = claims.getExpiration();
                        break;
                    default:
                        // Pour tous les autres claims (id, email, roles, etc.)
                        // on les récupère directement depuis la map des claims.
                        if (claims.containsKey(claimName)) {
                            value = claims.get(claimName);
                        }
                        break;
                }

                // On ajoute la valeur au résultat si elle n'est pas nulle
                if (value != null) {
                    tokenInfo.put(claimName, value);
                }
            }
            return tokenInfo;
        } catch (JwtException e) {
            logger.error("Error extracting token claims for audit: {}", e.getMessage());
            return Collections.emptyMap(); // Renvoyer une map vide est plus sûr
        }
    }

}
