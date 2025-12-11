package com.halaq.backend.core.config;

import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil; // ✅ Utilise JwtTokenUtil (pas JwtTokenProvider)

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        try {
            String token = null;

            // ✅ Essayer 1: Extraire le token du header Authorization
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                System.out.println("🔑 [STOMP] Token reçu du header Authorization");
            }

            // ✅ Essayer 2: Extraire le token du query parameter (fallback)
            if (token == null || token.isEmpty()) {
                String query = request.getURI().getQuery();
                if (query != null && query.contains("token=")) {
                    token = query.split("token=")[1].split("&")[0];
                    System.out.println("🔑 [STOMP] Token reçu du query parameter");
                }
            }

            if (token == null || token.isEmpty()) {
                System.err.println("❌ [STOMP] Token manquant ou invalide");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            System.out.println("🔑 [STOMP] Token reçu: " + token.substring(0, Math.min(20, token.length())) + "...");

            // ✅ Valider le token (méthode de JwtTokenUtil)
            try {
                if (!jwtTokenUtil.validateToken(token)) {
                    System.err.println("❌ [STOMP] Token invalide");
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
            } catch (ExpiredJwtException e) {
                System.err.println("❌ [STOMP] Token expiré");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // ✅ Extraire l'username du token
            String username = jwtTokenUtil.getUsernameFromToken(token);
            Long userId = jwtTokenUtil.getUserIdFromToken(token);

            System.out.println("✅ [STOMP] Authentification réussie");
            System.out.println("   └─ Username: " + username);
            System.out.println("   └─ UserId: " + userId);

            // ✅ Stocker dans les attributs pour utilisation dans les handlers
            attributes.put("username", username);
            attributes.put("userId", userId);
            attributes.put("token", token);

            return true;

        } catch (Exception e) {
            System.err.println("❌ [STOMP] Erreur authentification: " + e.getMessage());
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            System.err.println("❌ [STOMP] Erreur après handshake: " + exception.getMessage());
            exception.printStackTrace();
        } else {
            System.out.println("✅ [STOMP] Handshake complété avec succès");
        }
    }
}