package com.halaq.backend.payment.service;

import com.halaq.backend.core.audit.AuditLog;
import com.halaq.backend.core.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional
    public void log(Long userId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .action(action)
            .details(details)
            .level("INFO")
            .ipAddress(getCurrentUserIpAddress())
            .timestamp(LocalDateTime.now())
            .build();
        
        auditLogRepository.save(auditLog);
        log.info("Audit log: user={}, action={}, details={}", userId, action, details);
    }
    
    @Transactional
    public void logError(Long userId, String action, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .action(action)
            .details("ERROR: " + errorMessage)
            .level("ERROR")
            .ipAddress(getCurrentUserIpAddress())
            .timestamp(LocalDateTime.now())
            .build();
        
        auditLogRepository.save(auditLog);
        log.error("Audit error log: user={}, action={}, error={}", userId, action, errorMessage);
    }
    
    private String getCurrentUserIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }
}

