package com.halaq.backend.payment.entity;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.shared.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // RECHARGE, PAIEMENT, REMBOURSEMENT

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // CARD, MOMO, CASH, WALLET

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String encryptedCardToken; // Token sécurisé Stripe

    private String stripePaymentIntentId;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
