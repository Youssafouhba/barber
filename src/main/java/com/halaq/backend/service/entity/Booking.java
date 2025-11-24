package com.halaq.backend.service.entity;

import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Booking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private double destinationLatitude;
    private double destinationLongitude;
    private String destinationAddress;

    // NOUVEAU: Durée totale de la réservation (somme de la durée de tous les services)
    private Integer totalDurationInMinutes;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToMany
    @JoinTable(
            name = "booking_service",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<OfferedService> services;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Review review;
}
