package com.halaq.backend.core.transverse.cloud.exception;
public class BucketNotFoundException extends RuntimeException {
    public BucketNotFoundException(String message) {
        super(message);
    }
}
