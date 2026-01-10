package com.halaq.backend.payment.exception;

public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException() {
        super("Invalid webhook signature");
    }
}

