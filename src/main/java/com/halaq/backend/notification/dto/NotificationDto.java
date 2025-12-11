package com.halaq.backend.notification.dto;

import com.halaq.backend.notification.enums.NotificationStatus;
import com.halaq.backend.notification.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private String metadata;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
