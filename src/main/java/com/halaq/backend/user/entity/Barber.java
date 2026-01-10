package com.halaq.backend.user.entity;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.service.entity.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@DiscriminatorValue("BARBER")
@DynamicUpdate
public class Barber extends User {
    private Integer age;
    private Integer yearsOfExperience;
    private LocalDate birthDate;
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;
    private Integer currentStep = 0;
    private Boolean isAvailable = true;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ServiceZone> zones;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "barber_portfolio", joinColumns = @JoinColumn(name = "barber_id"))
    @Column(name = "image_url")
    private List<String> portfolio;


    private String barberShopName;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Document> documents;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Availability> availability;

    @OneToMany(mappedBy = "barber",fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "barber",fetch = FetchType.LAZY)
    private List<Review> reviews;

    // Représente le catalogue de services spécifiques à ce coiffeur
    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OfferedService> offeredServices;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TimeBlock> timeBlocks;
    
    // Payout information
    private String payoutMethod; // STRIPE, MOMO, etc.
    private String stripeAccountId;
    private String momoPhoneNumber;

}