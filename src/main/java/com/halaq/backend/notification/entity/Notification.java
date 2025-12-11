package com.halaq.backend.notification.entity;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.notification.enums.NotificationStatus;
import com.halaq.backend.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(length = 2000)
    private String metadata; // JSON string for extra data

    private LocalDateTime readAt;

    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
