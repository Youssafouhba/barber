package com.halaq.backend.core.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CsvUtil {

    public Map<String, Object> readCsv(MultipartFile[] files) throws IOException {
        List<String[]> allLines = new ArrayList<>();
        List<String> headers = null;

        for (MultipartFile file : files) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                // Lire l'en-tête
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    headers = Arrays.asList(headerLine.split(","));
                }
                // Lire toutes les lignes
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    // Nettoyer les valeurs
                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                        System.out.println(values[i]);
                    }
                    allLines.add(values);
                }

            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("headers", headers);
        response.put("lines", allLines);
        return response;
    }
}
