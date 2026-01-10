package com.halaq.backend.user.service.impl;

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

import java.util.ArrayList;
import java.util.List;

@Service
public class BarberLiveLocationsServiceImpl implements BarberLiveLocationsService {

    private final StringRedisTemplate redisTemplate;

    // Le nom de la clé dans Redis qui contiendra toutes les positions
    private static final String BARBER_LOCATIONS_KEY = "barbers:live_locations";

    public BarberLiveLocationsServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
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
