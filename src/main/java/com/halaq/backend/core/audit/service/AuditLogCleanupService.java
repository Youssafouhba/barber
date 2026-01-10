package com.halaq.backend.core.audit.service;

import com.halaq.backend.core.audit.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service de nettoyage des logs d'audit
 * Conservation des logs: 12 mois
 * Exécution: 1er du mois à 2h
 */
@Service
@Slf4j
public class AuditLogCleanupService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Scheduled(cron = "0 0 2 1 * ?") // 1er du mois à 2h
    @Transactional
    public void deleteOldAuditLogs() {
        log.info("Starting audit log cleanup...");
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(12);
        int deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Deleted {} old audit logs (older than 12 months)", deletedCount);
    }
}

