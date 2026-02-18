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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


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

        if (type == NotificationType.IN_APP) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
            sendExternalNotification(notification);
        }
        NotificationDto dto = notificationMapper.toDto(notificationRepository.save(notification));
        Long userId = recipient.getId();

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", dto);
        return dto;
    }

    // 🔥 NOUVELLE MÉTHODE : Envoi différé
    @Async
    public void scheduleWebSocketNotification(Notification notification, long delayMillis) {
        try {
            log.info("📅 Scheduling WebSocket notification in {}ms for user: {}",
                    delayMillis, notification.getRecipient().getUsername());

            Thread.sleep(delayMillis);

            sendToWebSocket(notification);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Notification scheduling interrupted", e);
        }
    }

    private void sendToWebSocket(Notification notification) {
        try {
            NotificationDto dto = notificationMapper.toDto(notification);
            String recipientUsername = notification.getRecipient().getUsername().trim();

            log.info("🔔 Envoi WS vers User: [{}]", recipientUsername);
            log.info("🔔 Destination: /user/{}/queue/notifications", recipientUsername);

            // 🔥 VÉRIFIEZ SI LE USER EST CONNECTÉ
            String destination = "/user/" + recipientUsername + "/queue/notifications";

            messagingTemplate.convertAndSend(
                    destination,
                    dto
            );

            log.info("✅ Notification envoyée via WebSocket à {}", destination);

        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket notification", e);
            // 🔥 OPTION: Stocker pour re-tentative plus tard
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    // 🔥 OPTIONNEL: Méthode pour vérifier si un user est connecté
    public boolean isUserConnected(String username) {
        // Cette méthode nécessite d'accéder à la session registry
        // Voir l'étape 2 ci-dessous
        return true; // Temporaire
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

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues
     * @param userId L'ID de l'utilisateur
     * @return Le nombre de notifications mises à jour
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("📚 Marquage de toutes les notifications comme lues pour l'utilisateur ID: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = notificationRepository.markAllAsReadByUserId(userId, now);

        log.info("✅ {} notifications marquées comme lues", updatedCount);


        return updatedCount;
    }

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues
     * @return Le nombre de notifications mises à jour
     */
    @Transactional
    public int markAllAsRead() {
        User recipient = getCurrentUser();
        if (recipient == null || recipient.getId() == null) {
            log.error("❌ Utilisateur invalide");
            return 0;
        }
        return markAllAsRead(recipient.getId());
    }


    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipient_IdAndReadAtIsNull(userId);
    }

    public void deleteById(Long id) {
         notificationRepository.deleteById(id);
    }
}
