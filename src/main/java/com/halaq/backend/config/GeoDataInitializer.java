package com.halaq.backend.config;

import com.halaq.backend.user.service.facade.BarberLiveLocationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initialise les données géographiques dans Redis au démarrage de l'application.
 * Charge toutes les zones de service depuis la base de données.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeoDataInitializer {

    private final BarberLiveLocationsService barberLiveLocationsService;

    /**
     * Exécuté automatiquement après le démarrage de l'application.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeGeoData() {
        log.info("=== Initialisation des données géographiques ===");
        try {
            barberLiveLocationsService.initializeServiceZonesInRedis();
            log.info("✓ Initialisation géographique complétée avec succès");
        } catch (Exception e) {
            log.error("✗ Erreur lors de l'initialisation des données géographiques", e);
            // Ne pas bloquer le démarrage si Redis n'est pas disponible
        }
    }
}