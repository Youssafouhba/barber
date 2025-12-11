package com.halaq.backend.notification.repository;

import com.halaq.backend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);
    
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    long countByRecipient_IdAndReadAtIsNull(Long recipientId);
}
