package com.halaq.backend.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Random;

public class LiveLocationUpdaterWithAuth {

    private static final String BASE_URL = "https://7769-105-71-16-25.ngrok-free.app/api/v1/barbers/live-location";
    private static final String AUTH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0JBUkJFUiJdLCJpZCI6MjAyLCJlbWFpbCI6Im91aGJhQGdtYWlsLmNvbSIsImVuYWJsZWQiOnRydWUsInVzZXJuYW1lIjoiT3VoYmEgWW91c3NlZiAiLCJzdWIiOiJPdWhiYSBZb3Vzc2VmICIsImlhdCI6MTc3MTA5MzMxMCwiZXhwIjoxNzcxMDk2OTEwfQ._RmXu-QXDvzEgi1JOwv173WYfimf5BX0uSZ_qVGtsc5xtuO3rDWb6Lnou1blwU6L_-9qtCrmf7k2Fcv2O6XPoA";
    private static final int UPDATE_INTERVAL_MS = 30000;

    private static double currentLat = 34.0209;
    private static double currentLon = -6.8416;
    private static final double DEST_LAT = 34.0033;
    private static final double DEST_LON = -6.8485;

    private static final Random random = new Random();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        // IMPORTANT: Forcer la locale US pour utiliser le point comme séparateur décimal
        Locale.setDefault(Locale.US);

        System.out.println("🚀 Démarrage avec authentification...\n");
        System.out.printf("📍 Position initiale: %.6f, %.6f%n", currentLat, currentLon);
        System.out.printf("🎯 Destination: %.6f, %.6f%n%n", DEST_LAT, DEST_LON);

        while (true) {
            try {
                updatePosition();
                Thread.sleep(UPDATE_INTERVAL_MS);
            } catch (InterruptedException e) {
                System.err.println("❌ Arrêt du programme");
                break;
            }
        }
    }

    private static void updatePosition() {
        try {
            // Calculer nouvelle position
            currentLat += (DEST_LAT - currentLat) * 0.05;
            currentLon += (DEST_LON - currentLon) * 0.05;

            // Variation aléatoire
            currentLat += (random.nextDouble() - 0.5) * 0.0001;
            currentLon += (random.nextDouble() - 0.5) * 0.0001;

            // SOLUTION 1: Utiliser String.format avec Locale.US
            String url = String.format(Locale.US, "%s?latitude=%.6f&longitude=%.6f",
                    BASE_URL, currentLat, currentLon);

            System.out.println("🔗 URL: " + url); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + AUTH_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                System.out.printf("✅ Position mise à jour: Lat=%.6f, Lon=%.6f%n%n",
                        currentLat, currentLon);
            } else {
                System.out.printf("⚠️  Code HTTP: %d%n", response.statusCode());
                System.out.println("📄 Réponse: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}