package com.halaq.backend.user.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "barber_live_locations")
public class BarberLiveLocations {
    @Id
    private Long barberId; // On utilise l'ID du barbier comme clé primaire

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Lie ce 'barberId' à l'entité Barber
    @JoinColumn(name = "barber_id")
    private Barber barber;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private Float heading;
    private Float speed;
    private Timestamp timestamp;
    private LocalDateTime lastUpdate;
    // Optionnel: pour savoir quelle course il suit
    private Long currentBookingId;
}
