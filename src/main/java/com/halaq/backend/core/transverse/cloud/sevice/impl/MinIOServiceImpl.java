package com.halaq.backend.core.transverse.cloud.sevice.impl;

import com.halaq.backend.core.transverse.cloud.config.MinioProperties;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.transverse.cloud.exception.BucketNotFoundException;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.VersioningConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinIOServiceImpl implements MinIOService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOServiceImpl.class);
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinIOServiceImpl(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public int saveBucket(String bucket) {
        if (bucketExists(bucket))
            return 0;
        try {
            // Création du bucket
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            logger.info("Bucket '{}' created successfully.", bucket);

            // Activation du versioning (comme dans l'implémentation originale)
            VersioningConfiguration config = new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, false);
            minioClient.setBucketVersioning(SetBucketVersioningArgs.builder().bucket(bucket).config(config).build());
            logger.info("Versioning enabled for bucket '{}'.", bucket);

            return 1;
        } catch (Exception e) {
            logger.error("Error creating bucket '{}'.", bucket, e);
            return 0;
        }
    }

    @Override
    public Optional<FileUploadResult> uploadFile(MultipartFile file) {
        // Délègue à la méthode à deux arguments qui contient déjà la logique de vérification/création.
        return uploadFile(file, minioProperties.getBucketName());
    }

    @Override
    public Optional<FileUploadResult> uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            // 🎯 VÉRIFIE ET CRÉE LE BUCKET S'IL N'EXISTE PAS
            if(!bucketExists(bucketName)) {
                saveBucket(bucketName);
            }

            logger.info("Uploading file: {} to bucket: {}", objectName, bucketName);

            // Upload le fichier avec le objectName spécifique
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Construire l'URL du fichier
            String fileUrl = generatePresignedUrl( bucketName, objectName);
            logger.info("File uploaded successfully: {}", fileUrl);

            FileUploadResult result = FileUploadResult.builder()
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .etag("") // ETag can be fetched if needed
                    .url(fileUrl)
                    .fileSize(file.getSize())
                    .build();

            return Optional.of(result);

        } catch (MinioException | java.io.IOException e) {
            logger.error("Error uploading file to Minio bucket '{}'", bucketName, e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileUploadResult> uploadFile(MultipartFile file, String bucketName) {
        try {
            // 🎯 VÉRIFIE ET CRÉE LE BUCKET S'IL N'EXISTE PAS
            if(!bucketExists(bucketName)) {
                saveBucket(bucketName);
            }
            String originalFilename = file.getOriginalFilename();
            String objectName = UUID.randomUUID() + "_" + originalFilename;

            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = generatePresignedUrl(bucketName, objectName);

            FileUploadResult result = new FileUploadResult(
                    response.bucket(),
                    response.object(),
                    response.etag(),
                    url,
                    file.getSize()
            );
            logger.info("Successfully uploaded file '{}' to bucket '{}'", objectName, bucketName);
            return Optional.of(result);

        } catch (Exception e) {
            logger.error("Error uploading file to Minio bucket '{}'", bucketName, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<byte[]> downloadFile(String objectName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectName)
                        .build())) {
            byte[] content = stream.readAllBytes();
            logger.info("Successfully downloaded file '{}'", objectName);
            return Optional.of(content);
        } catch (Exception e) {
            logger.error("Error downloading file '{}'", objectName, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteFile(String objectName, String bucketName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("Successfully deleted file '{}' from bucket '{}'", objectName, bucketName);
        } catch (Exception e) {
            logger.error("Error deleting file '{}' from MinIO bucket '{}'", objectName, bucketName, e);
            throw new RuntimeException("Error deleting file from MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] LoadDocument(String bucket, String documentName) throws MinioException {
        if (!bucketExists(bucket)) {
            throw new BucketNotFoundException("The bucket " + bucket + " does not exist");
        }
        try {
            // Get the document object from MinIO
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(documentName)
                            .build()
            );
            // Get the input stream containing the document data
            InputStream documentStream = response;
            // Create a byte array output stream to hold the document data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Buffer for reading data
            byte[] buffer = new byte[8192];
            int bytesRead;
            // Write the document data to the byte array output stream
            while ((bytesRead = documentStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            // Close the input stream for the document
            documentStream.close();

            // Return the document data as a byte array
            return baos.toByteArray();
        } catch (Exception e) {
            throw new MinioException("Error while downloading the document " + documentName + " from the bucket " + bucket + ", error : " + e.getMessage());
        }
    }

    @Override
    public boolean bucketExists(String name) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        } catch (Exception e) {
            logger.error("Error checking existence of bucket '{}'", name, e);
            return false;
        }
    }

    public boolean objectExists(String bucketName, String objectName) throws MinioException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true; // Object exists
        } catch (MinioException e) {
            // Check if the exception is due to object not found
            if (e instanceof ErrorResponseException && ((ErrorResponseException) e).errorResponse().code().equals("NoSuchKey")) {
                return false; // Object not found
            }
            // If it's not NoSuchKey, throw the MinioException
            throw e;
        } catch (Exception e) {
            throw new MinioException("An error occurred while checking if the object " + objectName + " exists: " + e.getMessage());
        }
    }


    private String generatePresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            logger.warn("Could not generate presigned URL for object '{}'", objectName, e);
            return null; // or return a public URL if your bucket is public
        }
    }


}
