package com.halaq.backend.notification.service;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.notification.dto.NotificationDto;
import com.halaq.backend.notification.entity.Notification;
import com.halaq.backend.notification.enums.NotificationStatus;
import com.halaq.backend.notification.enums.NotificationType;
import com.halaq.backend.notification.mapper.NotificationMapper;
import com.halaq.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDto createNotification(User recipient, String title, String message, NotificationType type, String metadata) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setMetadata(metadata);
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);
        
        // Send via WebSocket for all types (as a real-time update)
        sendToWebSocket(notification);

        if (type == NotificationType.IN_APP) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
             // Placeholder for external sending logic
             sendExternalNotification(notification);
        }

        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    private void sendToWebSocket(Notification notification) {
        try {
            NotificationDto dto = notificationMapper.toDto(notification);
            // Send to /user/{userId}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getUsername(), // Or ID depending on config, usually username for Spring Security
                    "/queue/notifications",
                    dto
            );
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    private void sendExternalNotification(Notification notification) {
        try {
            log.info("Sending {} notification to user {}: {}", notification.getType(), notification.getRecipient().getId(), notification.getTitle());
            // Simulate sending
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            notification.setStatus(NotificationStatus.FAILED);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toDto);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setReadAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadAtIsNull(userId);
    }
}
