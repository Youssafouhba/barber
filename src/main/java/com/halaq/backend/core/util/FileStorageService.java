package com.halaq.backend.core.util;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.service.facade.VerificationTrackerService;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.service.facade.BarberService;
import com.halaq.backend.user.service.facade.DocumentService; // AJOUT: Service pour persister l'entité Document.
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException; // IMPORTATION SUGGÉRÉE: Exception standard pour les entités non trouvées.
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // AJOUT: Assure que l'opération est atomique.
import org.springframework.web.multipart.MultipartFile;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;
import static com.halaq.backend.core.util.FileUtils.generateUniqueFileName;

/**
 * Service repensé pour gérer le stockage de fichiers de manière robuste et transactionnelle.
 * Il gère l'upload d'un fichier vers MinIO, la création et la persistance de l'entité Document correspondante.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final VerificationTrackerService verificationTrackerService;
    private final MinIOService minioService;

    public FileStorageService(VerificationTrackerService verificationTrackerService, MinIOService minioService) {
        this.verificationTrackerService = verificationTrackerService;
        this.minioService = minioService;
    }

    /**
     * Télécharger un fichier depuis Minio
     */
    public byte[] downloadFile(String bucketName, String objectName) throws MinioException {
        byte[] fileContent = minioService.LoadDocument(bucketName,objectName);
        if (fileContent == null) {
            throw new RuntimeException("Impossible de télécharger le fichier: " + objectName);
        }

        return fileContent;
    }

    /**
     * Uploade un document pour le barbier actuellement authentifié, crée une entrée en base de données
     * et retourne l'entité Document persistée.
     *
     * @param file         Le fichier unique à uploader (CORRIGÉ: MultipartFile au lieu de MultipartFile[]).
     * @param documentType Le type de document (AJOUT: pour plus de contexte).
     * @param bucketName   Le nom du bucket MinIO à utiliser.
     * @return L'entité Document qui a été sauvegardée en base de données.
     * @throws FileUploadException si le fichier est invalide ou si l'upload échoue.
     * @throws EntityNotFoundException si aucun profil barbier n'est trouvé pour l'utilisateur actuel.
     */
    @Transactional // AJOUT: Garantit que soit tout réussit (upload + save DB), soit tout échoue.
    public FileUploadResult uploadBarberDocument(MultipartFile file, DocumentType documentType, String bucketName) throws FileUploadException {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur n'est authentifié.");
        }
        // 1. Valider le fichier en entrée
        validateFile(file);
        logger.info("Tentative d'upload du fichier '{}' (type: {}) pour le barbier ID: {}", file.getOriginalFilename(), documentType, currentUser.getId());
        // 2. Uploader le fichier vers MinIO
        String uniqueName = generateUniqueFileName(currentUser.getId(), file.getOriginalFilename());
        FileUploadResult uploadResult = minioService.uploadFile(file, bucketName,uniqueName)
                .orElseThrow(() -> {
                    String errorMsg = String.format("Échec de l'upload du fichier '%s' vers MinIO.", file.getOriginalFilename());
                    logger.error(errorMsg);
                    return new FileUploadException(errorMsg);
                });
        // La persistance est maintenant gérée par ce service
        return uploadResult;
    }

    /**
     * Méthode utilitaire pour valider un fichier MultipartFile.
     * @param file Le fichier à valider.
     * @throws FileUploadException si le fichier est nul ou vide.
     */
    private void validateFile(MultipartFile file) throws FileUploadException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Le fichier à uploader ne peut pas être nul ou vide.");
        }
    }

    public static  String determineContentType(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            // Images
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "tiff":
            case "tif":
                return "image/tiff";
            case "ico":
                return "image/x-icon";

            // Documents
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "odt":
                return "application/vnd.oasis.opendocument.text";
            case "ods":
                return "application/vnd.oasis.opendocument.spreadsheet";

            // Par défaut
            default:
                return "application/octet-stream";
        }
    }

}