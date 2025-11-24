package com.halaq.backend.user.service.impl;

import com.halaq.backend.core.exception.EntityNotFoundException;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.service.facade.VerificationTrackerService;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.util.FileStorageService;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.shared.VerificationStatus;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.criteria.DocumentCriteria;
import com.halaq.backend.user.repository.DocumentRepository;
import com.halaq.backend.user.service.facade.BarberService;
import com.halaq.backend.user.service.facade.DocumentService;
import com.halaq.backend.user.specification.DocumentSpecification;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Service
public class DocumentServiceImpl extends AbstractServiceImpl<Document, DocumentCriteria, DocumentRepository> implements DocumentService {
    private final FileStorageService fileStorageService;
    private final BarberService barberService;
    private final VerificationTrackerService verificationTrackerService;
    @Value("${app.minio.bucket.diplomas}")
    private String bucketDiplomas;
    @Value("${app.minio.bucket.certifications}")
    private String bucketCertifications;
    @Value("${app.minio.bucket.cins}")
    private String bucketCins;
    @Value("${app.minio.bucket.portfolios}")
    private String bucketPortfolios;
    @Value("${app.minio.bucket.avatars}")
    private String bucketAvatars;


    public DocumentServiceImpl(DocumentRepository dao, FileStorageService fileStorageService, BarberService barberService, VerificationTrackerService verificationTrackerService) {
        super(dao);
        this.fileStorageService = fileStorageService;
        this.barberService = barberService;
        this.verificationTrackerService = verificationTrackerService;
    }

    @Override
    @Transactional
    public Document create(Document document) {
        // Business logic: New documents always start as PENDING verification
        document.setVerificationStatus(VerificationStatus.PENDING);
        return super.create(document);
    }

    @Override
    public void configure() {
        super.configure(Document.class, DocumentSpecification.class);
    }

    @Override
    public List<Document> uploadMultiple(MultipartFile[] files, DocumentType documentType) throws FileUploadException {
        // 2. Récupérer le barbier associé à l'utilisateur connecté
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur n'est authentifié.");
        }
        Barber barber = barberService.findById(currentUser.getId());
        if (barber == null) {
            throw new EntityNotFoundException("Aucun profil barbier trouvé pour l'utilisateur avec l'ID: " + currentUser.getId());
        }
        VerificationTracker tracker = verificationTrackerService.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Tracker not found"));

        List<Document> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            try {
                FileUploadResult fileUploadResult = fileStorageService.uploadBarberDocument(file, documentType, getBucketName(documentType.name()));
                // Créer, peupler et sauvegarder l'entité Document
                Document document = new Document();
                document.setBarber(barber);
                document.setName(file.getOriginalFilename());
                document.setSize(file.getSize());
                document.setType(documentType);
                document.setUrl(fileUploadResult.getObjectName());
                updateTracker(documentType.name(), tracker);
                documents.add(super.create(document));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return documents;
    }

    @Override
    public byte[] downloadFile(Long docId) {
        Document document = findById(docId);
        if (document == null) {
            throw new RuntimeException("Document not found");
        }
        String bucketName = getBucketName(document.getType().name());
        String objectName = document.getUrl();
        try {
            return fileStorageService.downloadFile(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Document> uploadPortfolioImage(MultipartFile[] files) throws FileUploadException {
        return uploadMultiple(files, DocumentType.PORTFOLIO);
    }
    private VerificationTracker updateTracker(String type,VerificationTracker tracker) {
        if (type.equals(DocumentType.DIPLOMA.name())) {
            tracker.setDocumentsVerified(true);
            tracker.setDocumentsVerifiedAt(java.time.LocalDateTime.now());
        }
        if (type.equals(DocumentType.CIN.name())) {
            tracker.setCinVerified(true);
            tracker.setCinVerifiedAt(java.time.LocalDateTime.now());
        }
        if (type.equals(DocumentType.CERTIFICATION.name())) {
            tracker.setCertificationVerified(true);
            tracker.setCertificationVerifiedAt(java.time.LocalDateTime.now());
        }
        if (type.equals(DocumentType.PORTFOLIO.name())) {
            tracker.setPortfolioVerified(true);
            tracker.setPortfolioVerifiedAt(java.time.LocalDateTime.now());
        }
        verificationTrackerService.saveTracker(tracker);
        return tracker;
    }


    private String getBucketName(String type) {
        if (type.equals(DocumentType.DIPLOMA.name())) {
            return bucketDiplomas;
        }
        if (type.equals(DocumentType.CIN.name())) {
            return bucketCins;
        }
        if (type.equals(DocumentType.CERTIFICATION.name())) {
            return bucketCertifications;
        }
        if (type.equals(DocumentType.PORTFOLIO.name())) {
            return bucketPortfolios;
        }
        return "documents";
    }

}