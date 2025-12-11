package com.halaq.backend.shared;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BarberMovementSimulator {

    // 1. CONFIGURATION
    // Remplace par le token du barbier que tu veux simuler (Login d'abord pour l'obtenir)
    private static final String BARBER_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0JBUkJFUiJdLCJpZCI6NjA4LCJlbWFpbCI6InlvdXNzZWZvdWhiYUBnbWFpbC5jb20iLCJlbmFibGVkIjp0cnVlLCJ1c2VybmFtZSI6IlVzZXIgVGVzdCIsInN1YiI6IlVzZXIgVGVzdCIsImlhdCI6MTc2NTM4ODk4NiwiZXhwIjoxNzY1MzkyNTg2fQ.drXX3qF36THWo8IIo-vRzbiMDXyXv6LNBdBcNPrsvTO1cFprbrRi1_cPXQ7qEPtTdwYrFNFfmfHApcsx5XefTg";
    private static final String BASE_URL = "https://1cf65d504da7.ngrok-free.app/api/v1/barbers/live-location";
    private static final int DELAY_MS = 5000; // 5 secondes entre chaque point

    // 2. LES COORDONNÉES (Hay Targa -> Destination)
    // Copié depuis notre génération précédente
    private static final double[][] ROUTE = {
            {31.656800, -8.033600},
            {31.656277, -8.032903},
            {31.655755, -8.032207},
            {31.655232, -8.031510},
            {31.654709, -8.030814},
            {31.654186, -8.030117},
            {31.653664, -8.029421},
            {31.653141, -8.028724},
            {31.652618, -8.028028},
            {31.652095, -8.027331},
            {31.651573, -8.026634},
            {31.651050, -8.025938},
            {31.650527, -8.025241},
            {31.650004, -8.024545},
            {31.649482, -8.023848},
            {31.648959, -8.023152},
            {31.648436, -8.022455},
            {31.647914, -8.021759},
            {31.647391, -8.021062},
            {31.646868, -8.020366}
    };

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("🚗 Démarrage de la simulation du trajet Barbier...");
        System.out.println("📍 Départ : Hay Targa");
        System.out.println("⏱️  Points : " + ROUTE.length);

        for (int i = 0; i < ROUTE.length; i++) {
            double lat = ROUTE[i][0];
            double lon = ROUTE[i][1];

            try {
                // Construction de l'URL avec Query Params
                String url = String.format("%s?latitude=%s&longitude=%s", BASE_URL, lat, lon);

                // Création de la requête
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + BARBER_TOKEN)
                        .POST(HttpRequest.BodyPublishers.noBody()) // POST sans body (params dans URL)
                        .timeout(Duration.ofSeconds(5))
                        .build();

                // Envoi
                long startTime = System.currentTimeMillis();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Logs
                if (response.statusCode() == 200) {
                    System.out.printf("✅ [%d/%d] Position mise à jour : %f, %f%n", (i + 1), ROUTE.length, lat, lon);
                } else {
                    System.err.printf("❌ Erreur (%d) : %s%n", response.statusCode(), response.body());
                    // En cas d'erreur 401, on arrête car le token est invalide
                    if (response.statusCode() == 401) {
                        System.err.println("STOP : Token expiré ou invalide.");
                        break;
                    }
                }

                // Pause (Sauf pour le dernier point)
                if (i < ROUTE.length - 1) {
                    Thread.sleep(DELAY_MS);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Exception lors de l'envoi : " + e.getMessage());
            }
        }

        System.out.println("🏁 Trajet terminé ! Le barbier est arrivé à destination.");
    }
}