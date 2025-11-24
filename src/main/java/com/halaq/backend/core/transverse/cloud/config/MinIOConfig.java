package com.halaq.backend.core.transverse.cloud.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinIOConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinIOConfig.class);

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getUrl())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();

        // Create the default bucket on startup if it doesn't exist
        createBucketIfNotExists(minioClient, properties.getBucketName());

        return minioClient;
    }

    private void createBucketIfNotExists(MinioClient minioClient, String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("✅ Bucket '{}' created successfully.", bucketName);
            } else {
                logger.info("ℹ️ Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            logger.error("❌ Could not create or check bucket '{}'", bucketName, e);
            // In a real application, you might want to prevent the app from starting
            // throw new RuntimeException("Minio bucket creation failed", e);
        }
    }
}
