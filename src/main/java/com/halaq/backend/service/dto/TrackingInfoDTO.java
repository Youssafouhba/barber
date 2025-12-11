package com.halaq.backend.service.dto;

import com.halaq.backend.shared.BookingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackingInfoDTO {
    private Long bookingId;
    private BookingStatus status;

    // Infos Barber
    private Long barberId;
    private String barberName;
    private String barberPhone;
    private String barberAvatar; // URL ou nom fichier

    // Positions
    private double clientLatitude;
    private double clientLongitude;

    private double barberLatitude;
    private double barberLongitude;

    // Calculés
    private double distanceKm;     // Distance à vol d'oiseau ou route
    private String estimatedTime;  // "15 min" (Optionnel, calculé côté front ou via Google API ici)
}