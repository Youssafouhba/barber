package com.halaq.backend.core.storage;

import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service de base (CORE) pour la gestion du stockage de fichiers.
 * Sa seule responsabilité est d'interagir avec la couche de stockage (ex: MinIO).
 */
@Service
public class CoreFileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CoreFileStorageService.class);
    private final MinIOService minioService;

    public CoreFileStorageService(MinIOService minioService) {
        this.minioService = minioService;
    }

    /**
     * Uploade un fichier vers le bucket par défaut.
     *
     * @param file Le fichier à uploader.
     * @return Le résultat de l'upload contenant l'URL, le nom de l'objet, etc.
     * @throws FileUploadException si le fichier est invalide ou si l'upload échoue.
     */
    public FileUploadResult upload(MultipartFile file) throws FileUploadException {
        return this.upload(file, null); // Utilise le bucket par défaut défini dans MinIOService
    }

    /**
     * Uploade un fichier vers un bucket spécifique.
     *
     * @param file       Le fichier à uploader.
     * @param bucketName Le nom du bucket de destination. Si null, le bucket par défaut sera utilisé.
     * @return Le résultat de l'upload.
     * @throws FileUploadException si le fichier est invalide ou si l'upload échoue.
     */
    public FileUploadResult upload(MultipartFile file, String bucketName) throws FileUploadException {
        // 1. Valider le fichier
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Cannot upload an empty or null file.");
        }

        logger.info("Attempting to upload file '{}' to bucket '{}'", file.getOriginalFilename(), bucketName != null ? bucketName : "default");

        // 2. Déléguer l'upload au service MinIO
        // L'Optional est géré ici pour lancer une exception claire en cas d'échec.
        return minioService.uploadFile(file, bucketName)
                .orElseThrow(() -> {
                    String errorMsg = String.format("Failed to upload file '%s'. The storage service returned an empty result.", file.getOriginalFilename());
                    logger.error(errorMsg);
                    return new FileUploadException(errorMsg);
                });
    }

    /**
     * Supprime un fichier du bucket par défaut.
     *
     * @param objectName Le nom de l'objet (le nom du fichier sur MinIO) à supprimer.
     */
    public void delete(String objectName) {
        if (StringUtils.hasText(objectName)) {
            minioService.deleteFile(objectName,"default");
        }
    }
}