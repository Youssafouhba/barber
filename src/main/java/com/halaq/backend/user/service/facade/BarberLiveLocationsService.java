package com.halaq.backend.user.service.facade;


import org.springframework.data.geo.Point;

import java.util.List;


public interface BarberLiveLocationsService  {

    void initializeServiceZonesInRedis();

    List<Long> findNearbyBarberIdsByServiceZones(double userLat, double userLon, double radiusKm);

    void updateBarberLocation(Long barberId, double latitude, double longitude);

    List<Long> findNearbyBarberIds(double userLat, double userLon, double radiusKm);

    void removeBarberLocation(Long barberId);

    Point getBarberLocation(Long id);
}
