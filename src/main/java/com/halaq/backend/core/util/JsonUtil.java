package com.halaq.backend.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {

    private static ObjectMapper objectMapper;
    public JsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public static String toJson(Object conge) {
        try {
            return objectMapper.writeValueAsString(conge);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
