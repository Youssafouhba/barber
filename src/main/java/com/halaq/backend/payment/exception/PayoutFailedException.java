package com.halaq.backend.payment.exception;

public class PayoutFailedException extends RuntimeException {
    public PayoutFailedException(String message) {
        super(message);
    }
    
    public PayoutFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

