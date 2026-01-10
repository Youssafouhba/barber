package com.halaq.backend.service.entity;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.user.entity.Barber;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Représente un blocage ponctuel de temps par le coiffeur pour toute raison
 * (rendez-vous personnel, client externe, pause imprévue, etc.).
 * Permet de soustraire du temps à la disponibilité théorique.
 */
@Entity
@Getter
@Setter
public class TimeBlock extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Début exact du blocage (date et heure)
    private LocalDateTime startDateTime;

    // Fin exacte du blocage (date et heure)
    private LocalDateTime endDateTime;

    // Raison du blocage (ex: "Client sans appli", "Pause déjeuner", "Rendez-vous personnel")
    private String reason;

    // Relation ManyToOne avec le coiffeur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;
}


