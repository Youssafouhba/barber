package com.halaq.backend.user.entity;

import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Setter
@Table(name = "service_zones")
@DynamicUpdate
public class ServiceZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'ID unique de Google (ex: "ChIJSb1PhenSpw0RBVBX2CvIjcs")
    @Column(nullable = false)
    private String placeId;

    // Le nom simple (ex: "Maarif")
    @Column(nullable = false)
    private String name;

    // L'adresse complète (ex: "Maarif, Casablanca, Maroc")
    private String address;

    // Coordonnées du centre de la zone (utile pour les filtres)
    private Double latitude;
    private Double longitude;

    // Relation: Chaque zone appartient à un barbier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    public ServiceZone(long id, String name, String address, double latitude, double longitude, long barberId, String barberName, double distance) {

    }


    public ServiceZone() {

    }
}