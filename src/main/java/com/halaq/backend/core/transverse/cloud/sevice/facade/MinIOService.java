package com.halaq.backend.core.transverse.cloud.sevice.facade;

import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface MinIOService {

    int saveBucket(String bucket);

    /**
     * Uploads a file to the default Minio bucket.
     *
     * @param file The file to upload.
     * @return An Optional containing the result of the upload.
     */
    Optional<FileUploadResult> uploadFile(MultipartFile file);

    Optional<FileUploadResult> uploadFile(MultipartFile file, String bucketName, String objectName);

    /**
     * Uploads a file to a specific Minio bucket.
     *
     * @param file       The file to upload.
     * @param bucketName The name of the bucket to upload to.
     * @return An Optional containing the result of the upload.
     */
    Optional<FileUploadResult> uploadFile(MultipartFile file, String bucketName);

    /**
     * Downloads a file from the default bucket.
     *
     * @param objectName The name of the object (file) to download.
     * @return An Optional containing the file content as a byte array.
     */
    Optional<byte[]> downloadFile(String objectName);

    /**
     * Deletes a file from the default bucket.
     *
     * @param objectName The name of the object to delete.
     * @param bucketName The name of the bucket to delete the object from.
     */
    void deleteFile(String objectName, String bucketName);

    byte[] LoadDocument(String bucket, String documentName) throws MinioException;

    boolean bucketExists(String name);
}
