package com.halaq.backend.user.service.impl;

import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.repository.ServiceZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import com.halaq.backend.user.service.facade.BarberLiveLocationsService;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class BarberLiveLocationsServiceImpl implements BarberLiveLocationsService {

    private final ServiceZoneRepository serviceZoneRepository;

    private static final Logger log = LoggerFactory.getLogger(BarberLiveLocationsServiceImpl.class);

    private final StringRedisTemplate redisTemplate;

    // Le nom de la clé dans Redis qui contiendra toutes les positions
    private static final String BARBER_LOCATIONS_KEY = "barbers:live_locations";
    private static final String ZONE_GEO_KEY = "service_zones:geo"; // Clé principale pour toutes les zones
    private static final String ZONE_DATA_KEY = "service_zones:data"; // Hash pour stocker les détails
    private static final String BARBER_ZONES_KEY = "barber:zones"; // Ensemble des zones par barbier

    public BarberLiveLocationsServiceImpl(ServiceZoneRepository serviceZoneRepository, StringRedisTemplate redisTemplate) {
        this.serviceZoneRepository = serviceZoneRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Initialise ou met à jour les données géographiques des zones de service dans Redis.
     * À appeler au démarrage de l'app ou lors d'une modification de zone.
     *
     * Note: Les zones sont chargées avec les barbiers pour éviter
     * LazyInitializationException en dehors de la session Hibernate.
     */
    @Override
    public void initializeServiceZonesInRedis() {
        try {
            // Récupérer toutes les zones de service
            List<ServiceZone> allZones = serviceZoneRepository.findAll();

            // Force le chargement des barbiers DANS la session
            for (ServiceZone zone : allZones) {
                // Accéder à la relation pour la forcer à charger immédiatement
                if (zone.getBarber() != null) {
                    zone.getBarber().getId(); // Minimal access to initialize
                }
            }

            if (allZones.isEmpty()) {
                log.warn("Aucune zone de service trouvée dans la base de données");
                return;
            }

            // Nettoyer les anciennes données
            redisTemplate.delete(ZONE_GEO_KEY);
            redisTemplate.delete(ZONE_DATA_KEY);
            redisTemplate.delete(BARBER_ZONES_KEY);

            // Ajouter chaque zone à Redis
            for (ServiceZone zone : allZones) {
                addZoneToRedis(zone);
            }

            log.info("Initialisation Redis terminée avec {} zones de service", allZones.size());

        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation des zones dans Redis", e);
        }
    }
    /**
     * Ajoute ou met à jour une zone de service dans Redis.
    public void addZoneToRedis(ServiceZone zone) {

     */
    public void addZoneToRedis(ServiceZone zone) {
        try {
            if (zone.getLatitude() == null || zone.getLongitude() == null) {
                log.warn("Zone {} n'a pas de coordonnées", zone.getName());
                return;
            }

            // Créer un ID unique pour la zone (barber_id + zone_id)
            String zoneKey = generateZoneKey(zone);

            // Ajouter les coordonnées géographiques
            Point point = new Point(zone.getLongitude(), zone.getLatitude());
            redisTemplate.opsForGeo().add(ZONE_GEO_KEY, point, zoneKey);

            // Stocker les détails de la zone pour récupération ultérieure
            Map<String, String> zoneData = new HashMap<>();
            zoneData.put("id", zone.getId().toString());
            zoneData.put("name", zone.getName());
            zoneData.put("address", zone.getAddress() != null ? zone.getAddress() : "");
            zoneData.put("latitude", zone.getLatitude().toString());
            zoneData.put("longitude", zone.getLongitude().toString());
            zoneData.put("barber_id", zone.getBarber().getId().toString());
            zoneData.put("barber_name", zone.getBarber().getUsername());

            redisTemplate.opsForHash().putAll(ZONE_DATA_KEY + ":" + zoneKey, zoneData);

            // Mapper zone -> barber (utile pour filtrer par barbier)
            redisTemplate.opsForSet().add(
                    BARBER_ZONES_KEY + ":" + zone.getBarber().getId(),
                    zoneKey
            );

            log.debug("Zone {} ajoutée à Redis pour le barbier {}", zone.getName(), zone.getBarber().getUsername());

        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de la zone à Redis", e);
        }
    }

    /**
     * Supprime une zone de service de Redis.
     */
    public void removeZoneFromRedis(ServiceZone zone) {
        try {
            String zoneKey = generateZoneKey(zone);

            redisTemplate.opsForGeo().remove(ZONE_GEO_KEY, zoneKey);
            redisTemplate.delete(ZONE_DATA_KEY + ":" + zoneKey);
            redisTemplate.opsForSet().remove(
                    BARBER_ZONES_KEY + ":" + zone.getBarber().getId(),
                    zoneKey
            );

            log.debug("Zone {} supprimée de Redis", zone.getName());

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la zone de Redis", e);
        }
    }


    /**
     * Trouve les IDs des barbiers opérant dans des zones proches de la position donnée.
     *
     * @param userLat    Latitude de l'utilisateur
     * @param userLon    Longitude de l'utilisateur
     * @param radiusKm   Rayon de recherche en kilomètres
     * @return Liste des IDs des barbiers ayant une zone de service dans le rayon
     */
    @Override
    public List<Long> findNearbyBarberIdsByServiceZones(double userLat, double userLon, double radiusKm) {
        try {
            // Créer le cercle de recherche
            Point userLocation = new Point(userLon, userLat);
            Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
            Circle circle = new Circle(userLocation, radius);

            // Arguments de la commande GEOSEARCH
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .includeCoordinates()
                    .sortAscending();

            // Exécuter la recherche sur les zones de service
            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    redisTemplate.opsForGeo().radius(ZONE_GEO_KEY, circle, args);

            Set<Long> uniqueBarberIds = new HashSet<>();

            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                    String zoneKey = result.getContent().getName();

                    // Récupérer l'ID du barbier depuis les données de la zone
                    Map<Object, Object> zoneDataRaw = redisTemplate.opsForHash().entries(ZONE_DATA_KEY + ":" + zoneKey);
                    @SuppressWarnings("unchecked")
                    Map<String, String> zoneData = (Map<String, String>) (Map<?, ?>) zoneDataRaw;

                    if (zoneData != null && zoneData.containsKey("barber_id")) {
                        Long barberId = Long.parseLong(zoneData.get("barber_id"));
                        uniqueBarberIds.add(barberId);

                        double distance = result.getDistance().getValue();
                        log.debug("Zone trouvée: {} (distance: {:.2f}km) - Barbier: {}",
                                zoneData.get("name"), distance, zoneData.get("barber_name"));
                    }
                }
            }

            log.info("Trouvé {} barbiers avec des zones de service à proximité", uniqueBarberIds.size());
            return new ArrayList<>(uniqueBarberIds);

        } catch (Exception e) {
            log.error("Erreur lors de la recherche des barbiers par zones de service", e);
            return Collections.emptyList();
        }
    }


    /**
     * Trouve les zones de service proches avec tous leurs détails.
     *
     * @return Liste des DTO contenant les détails des zones trouvées
     */
    public List<ServiceZone> findNearbyServiceZonesWithDetails(double userLat, double userLon, double radiusKm) {
        try {
            Point userLocation = new Point(userLon, userLat);
            Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
            Circle circle = new Circle(userLocation, radius);

            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .sortAscending();

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    redisTemplate.opsForGeo().radius(ZONE_GEO_KEY, circle, args);

            List<ServiceZone> zones = new ArrayList<>();

            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                    String zoneKey = result.getContent().getName();
                    double distance = result.getDistance().getValue();

                    Map<Object, Object> zoneDataRaw = redisTemplate.opsForHash().entries(ZONE_DATA_KEY + ":" + zoneKey);
                    @SuppressWarnings("unchecked")
                    Map<String, String> zoneData = (Map<String, String>) (Map<?, ?>) zoneDataRaw;

                    if (zoneData != null) {
                        ServiceZone dto = new ServiceZone(
                                Long.parseLong(zoneData.get("id")),
                                zoneData.get("name"),
                                zoneData.get("address"),
                                Double.parseDouble(zoneData.get("latitude")),
                                Double.parseDouble(zoneData.get("longitude")),
                                Long.parseLong(zoneData.get("barber_id")),
                                zoneData.get("barber_name"),
                                distance
                        );
                        zones.add(dto);
                    }
                }
            }

            return zones;

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des détails des zones", e);
            return Collections.emptyList();
        }
    }


    /**
     * Met en cache la zone dans Redis (appeler après une modification en DB).
     */
    public void refreshZoneInRedis(Long zoneId) {
        try {
            ServiceZone zone = serviceZoneRepository.findById(zoneId)
                    .orElseThrow(() -> new RuntimeException("Zone non trouvée"));

            removeZoneFromRedis(zone);
            addZoneToRedis(zone);

            log.info("Zone {} rafraîchie dans Redis", zone.getName());
        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement de la zone {}", zoneId, e);
        }
    }

    /**
     * Génère une clé unique pour une zone de service.
     */
    private String generateZoneKey(ServiceZone zone) {
        return "zone:" + zone.getBarber().getId() + ":" + zone.getId();
    }

    /**
     * Met à jour la position du barber dans Redis.
     * À appeler via WebSocket ou API REST chaque fois que le téléphone du barber envoie une position.
     */
    @Override
    public void updateBarberLocation(Long barberId, double latitude, double longitude) {
        // ATTENTION : Redis prend (Longitude, Latitude) et non l'inverse !
        Point point = new Point(longitude, latitude);

        // Ajoute ou met à jour la position
        redisTemplate.opsForGeo().add(BARBER_LOCATIONS_KEY, point, String.valueOf(barberId));

        // Optionnel : On peut définir une expiration sur la localisation globale
        // mais c'est mieux de gérer la déconnexion manuellement.
    }

    /**
     * Récupère les IDs des barbers proches d'une position donnée.
     */
    @Override
    public List<Long> findNearbyBarberIds(double userLat, double userLon, double radiusKm) {
        // Définir le centre (Position du client)
        Point userLocation = new Point(userLon, userLat);

        // Définir la distance (Rayon)
        Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);

        // Créer le cercle de recherche
        Circle circle = new Circle(userLocation, radius);

        // Arguments de la commande GEOSEARCH
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance() // On veut savoir à quelle distance ils sont
                .sortAscending();  // Les plus proches en premier

        // Exécution de la recherche
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(BARBER_LOCATIONS_KEY, circle, args);

        List<Long> nearbyBarberIds = new ArrayList<>();

        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                // result.getContent().getName() retourne l'ID du barber (stocké en String)
                String barberIdStr = result.getContent().getName();
                nearbyBarberIds.add(Long.parseLong(barberIdStr));

                // Tu peux aussi récupérer la distance exacte ici si besoin :
                // double dist = result.getDistance().getValue();
            }
        }

        return nearbyBarberIds;
    }

    /**
     * Supprime la localisation d'un barber (quand il passe hors ligne ou "n'est plus disponible")
     */
    @Override
    public void removeBarberLocation(Long barberId) {
        redisTemplate.opsForGeo().remove(BARBER_LOCATIONS_KEY, String.valueOf(barberId));
    }

    @Override
    public Point getBarberLocation(Long barberId) {
        List<Point> positions = redisTemplate.opsForGeo().position(BARBER_LOCATIONS_KEY, String.valueOf(barberId));
        if (positions != null && !positions.isEmpty()) {
            return positions.get(0);
        }
        return null;
    }
}
