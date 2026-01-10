package com.halaq.backend.core.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CodeGenerationUtil {
    /**
     * Generates a random code of 6 digits
     */
    public String generateRandomCode() {
        int codeLength = 6;  // Or any desired length
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10)); // Generates a random digit
        }
        return sb.toString();
    }
}
