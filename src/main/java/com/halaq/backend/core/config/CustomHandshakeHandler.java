package com.halaq.backend.core.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Récupérer l'username stocké par le StompHandshakeInterceptor
        String username = (String) attributes.get("username");

        if (username != null) {
            // ✅ Important : On trim pour éviter les problèmes d'espaces vus dans vos logs
            return new StompPrincipal(username.trim());
        }
        return null;
    }
}