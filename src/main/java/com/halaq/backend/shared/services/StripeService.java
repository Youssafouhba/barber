package com.halaq.backend.shared.services;

import com.halaq.backend.payment.exception.PaymentProcessingException;
import com.halaq.backend.payment.exception.WebhookVerificationException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency,
                                             Long userId, String paymentMethodId, String description) {

        Stripe.apiKey = stripeApiKey;

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue()); // En cents
            params.put("currency", currency.toLowerCase());
            params.put("payment_method", paymentMethodId);
            params.put("confirm", true);
            params.put("description", description);
            params.put("metadata", Map.of("userId", userId.toString()));

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("PaymentIntent created: {}", intent.getId());

            return intent;
        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create payment intent", e);
        }
    }

    public Event verifyAndParseWebhook(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature");
            throw new WebhookVerificationException();
        }
    }
    
    public Transfer createTransfer(Long amountInCents, String currency, String destination, String description) {
        Stripe.apiKey = stripeApiKey;
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amountInCents);
            params.put("currency", currency.toLowerCase());
            params.put("destination", destination);
            params.put("description", description);
            
            Transfer transfer = Transfer.create(params);
            log.info("Transfer created: {}", transfer.getId());
            
            return transfer;
        } catch (StripeException e) {
            log.error("Stripe transfer error: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create transfer", e);
        }
    }
    
    public Transfer retrieveTransfer(String transferId) throws StripeException {
        Stripe.apiKey = stripeApiKey;
        return Transfer.retrieve(transferId);
    }
}