package com.halaq.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        // On utilise StringRedisTemplate pour stocker des clés et valeurs
        // sous forme de chaînes de caractères (ex: Clé: "barber:1", Valeur: "{\"lat\": ...}")
        return new StringRedisTemplate(connectionFactory);
    }
}
