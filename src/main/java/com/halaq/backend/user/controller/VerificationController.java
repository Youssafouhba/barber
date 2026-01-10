package com.halaq.backend.user.controller;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import com.halaq.backend.core.util.FileStorageService;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.user.converter.BarberConverter;
import com.halaq.backend.user.converter.DocumentConverter;
import com.halaq.backend.user.dto.DocumentDto;
import com.halaq.backend.user.dto.IdCardDataDto;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.service.facade.AsyncVerificationService;
import com.halaq.backend.user.service.facade.BarberService;
import com.halaq.backend.user.service.facade.DocumentService;
import com.halaq.backend.user.service.facade.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.*;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Tag(name = "Document Verification")
@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {
    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private final AsyncVerificationService asyncVerificationService;
    private final DocumentService documentService;
    private final DocumentConverter documentConverter;

    public VerificationController(AsyncVerificationService asyncVerificationService, DocumentService documentService, DocumentConverter documentConverter) {
        this.asyncVerificationService = asyncVerificationService;
        this.documentService = documentService;
        this.documentConverter = documentConverter;
    }


    @Operation(summary = "Uploads an ID card and starts OCR processing")
    @PostMapping(value = "/upload-id", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadIdCard(@RequestParam("files") MultipartFile[] files) {
        try {
           if (files.length == 0) {
               return ResponseEntity.badRequest().body("No files uploaded");
           }
            String taskId = UUID.randomUUID().toString();
            // Sauvegarder le fichier dans MinIO
            List<Document> documents = documentService.uploadMultiple(files, DocumentType.CIN);
            asyncVerificationService.processIdCardAsync(taskId, files[0]);
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("status", "PROCESSING");
            response.put("message", "OCR processing started");
            response.put("document", documentConverter.toDto(documents.get(0)));
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            logger.error("Error starting ID card processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. Endpoint pour vérifier le statut
    @GetMapping("/task-status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        String status = asyncVerificationService.getTaskStatus(taskId);

        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", status);

        if ("COMPLETED".equals(status)) {
            IdCardDataDto result = asyncVerificationService.getTaskResult(taskId);
            response.put("data", result);
        }

        return ResponseEntity.ok(response);
    }
}