package com.halaq.backend.notification.repository;

import com.halaq.backend.notification.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);
    
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    long countByRecipient_IdAndReadAtIsNull(Long recipientId);

    // ✅ NOUVELLE MÉTHODE: Marquer toutes comme lues
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt, n.status = 'READ' " +
            "WHERE n.recipient.id = :userId AND n.readAt IS NULL")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    // ✅ NOUVELLE MÉTHODE: Supprimer toutes les notifications lues
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :userId AND n.readAt IS NOT NULL")
    int deleteAllReadByUserId(@Param("userId") Long userId);

     void deleteById(Long id);

}
