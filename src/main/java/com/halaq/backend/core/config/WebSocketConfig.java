package com.halaq.backend.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private StompHandshakeInterceptor stompHandshakeInterceptor;

    @Bean(name = "wsHeartbeatScheduler")
    public ThreadPoolTaskScheduler wsHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        System.out.println("✅ TaskScheduler 'wsHeartbeatScheduler' créé");
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ SimpleBroker avec heartbeat
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{25000, 25000})
                .setTaskScheduler(wsHeartbeatScheduler());

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");

        System.out.println("✅ MessageBroker configuré");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(stompHandshakeInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler()) // ✅ AJOUTER CECI
                .withSockJS();

        System.out.println("✅ WebSocket endpoint /ws avec SockJS activé");
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // ✅ CORRECTION: Récupérer comme SimpMessageType (pas String)
                SimpMessageType messageType = (SimpMessageType) message.getHeaders().get("simpMessageType");
                String sessionId = (String) message.getHeaders().get("simpSessionId");

                if (messageType != null) {
                    System.out.println("📨 [INBOUND] " + messageType.name() + " | Session: " + sessionId);

                    if (SimpMessageType.CONNECT.equals(messageType)) {
                        System.out.println("   └─ Client connecting...");
                    } else if (SimpMessageType.SUBSCRIBE.equals(messageType)) {
                        String destination = (String) message.getHeaders().get("simpDestination");
                        System.out.println("   └─ Subscribing to: " + destination);
                    } else if (SimpMessageType.DISCONNECT.equals(messageType)) {
                        System.out.println("   └─ Client disconnecting");
                    }
                }
                return message;
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // ✅ CORRECTION: Récupérer comme SimpMessageType (pas String)
                SimpMessageType messageType = (SimpMessageType) message.getHeaders().get("simpMessageType");

                if (messageType != null) {
                    if (SimpMessageType.CONNECT_ACK.equals(messageType)) {
                        System.out.println("📤 [OUTBOUND] CONNECTED ✅ Client connecté!");
                    } else if (SimpMessageType.MESSAGE.equals(messageType)) {
                        String destination = (String) message.getHeaders().get("simpDestination");
                        System.out.println("📤 [OUTBOUND] MESSAGE → " + destination);
                    } else if (SimpMessageType.DISCONNECT_ACK.equals(messageType)) {
                        System.out.println("📤 [OUTBOUND] DISCONNECT_ACK");
                    }
                }
                return message;
            }
        });
    }
}