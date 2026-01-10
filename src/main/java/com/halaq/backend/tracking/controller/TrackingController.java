package com.halaq.backend.tracking.controller;

import com.halaq.backend.tracking.dto.UpdateLocationDto;
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson pour convertir en JSON
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.TimeUnit;

// (Pour la sécurité, simule l'ID du barbier.
// Plus tard, vous utiliserez @AuthenticationPrincipal)
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.halaq.backend.core.security.entity.User; // Votre entité User


@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    @Autowired
    private StringRedisTemplate redisTemplate; // Notre template configuré

    @Autowired
    private ObjectMapper objectMapper; // Pour convertir l'objet en JSON String

    private static final String KEY_PREFIX = "barber:location:";
    private static final int LOCATION_TTL_MINUTES = 5; // Temps d'expiration

    /**
     * Appelé par l'application du BARBIER pour mettre à jour sa position.
     */
    @PostMapping("/update")
    public ResponseEntity<Void> updateBarberLocation(
            @RequestBody UpdateLocationDto locationDTO,
            @AuthenticationPrincipal User authenticatedUser) { // Récupère le barbier connecté

        // Simule l'ID du barbier. REMPLACEZ CECI par la vraie sécurité
        // Long barberId = authenticatedUser.getId();
        Long barberId = 123L; // ID du barbier (pour test)

        try {
            // 1. Définir la clé unique pour ce barbier
            String redisKey = KEY_PREFIX + barberId;

            // 2. Convertir l'objet DTO en une chaîne JSON
            String jsonValue = objectMapper.writeValueAsString(locationDTO);

            // 3. Écrire dans Redis
            redisTemplate.opsForValue().set(redisKey, jsonValue);

            // 4. TRÈS IMPORTANT: Définir une expiration (TTL - Time To Live)
            // Si l'app du barbier crash, sa position sera supprimée après 5 min.
            redisTemplate.expire(redisKey, LOCATION_TTL_MINUTES, TimeUnit.MINUTES);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Gérer l'erreur (ex: Redis est inaccessible)
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Appelé par l'application du CLIENT pour obtenir la position du barbier.
     */
    @GetMapping("/{barberId}")
    public ResponseEntity<String> getBarberLocation(@PathVariable Long barberId) {

        String redisKey = KEY_PREFIX + barberId;

        // 1. Lire la valeur depuis Redis
        String jsonValue = redisTemplate.opsForValue().get(redisKey);

        if (jsonValue == null) {
            // 2. Si la clé n'existe pas (le barbier n'est pas en route ou l'app est crashée)
            // On renvoie "Not Found" pour que l'app client puisse le gérer.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Location not found.");
        }

        // 3. Renvoyer la position (qui est déjà un JSON)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON) // Précise qu'on renvoie du JSON
                .body(jsonValue);
    }
}