package com.halaq.backend.payment.entity;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.shared.PayoutStatus;
import com.halaq.backend.user.entity.Barber;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coiffeur_payouts")
@Getter
@Setter
public class CoiffeurPayout extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    private String externalReference;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;
}

