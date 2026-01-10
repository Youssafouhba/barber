package com.halaq.backend.core.transverse.cloud.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResult {
    // Getters
    private final String bucketName;
    private final String objectName;
    private final String etag;
    private final String url;
    private final long fileSize;

    public FileUploadResult(String bucketName, String objectName, String etag, String url, long fileSize) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.etag = etag;
        this.url = url;
        this.fileSize = fileSize;
    }

}
