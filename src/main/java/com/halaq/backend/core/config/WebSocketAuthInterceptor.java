package com.halaq.backend.core.config;

import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenUtil jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get token from query parameters (passed in handshake)
            String token = null;
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                token = (String) sessionAttributes.get("token");
            }

            // Alternatively, get from headers
            if (token == null) {
                List<String> tokenList = accessor.getNativeHeader("Authorization");
                if (tokenList != null && !tokenList.isEmpty()) {
                    token = tokenList.get(0);
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }
                }
            }

            if (token != null && jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                // Set authentication in the session
                accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
            } else {
                throw new AccessDeniedException("Invalid or missing token");
            }
        }

        return message;
    }
}