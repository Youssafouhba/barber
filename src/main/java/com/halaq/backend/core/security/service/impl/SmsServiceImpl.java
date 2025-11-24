package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.service.facade.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsServiceImpl implements SmsService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SmsServiceImpl.class);
    @Value("${app.sms.provider:mock}")
    private String smsProvider;

    // Twilio Configuration
    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.phone-number:}")
    private String twilioPhoneNumber;

    // Nexmo/Vonage Configuration
    @Value("${app.sms.nexmo.api-key:}")
    private String nexmoApiKey;

    @Value("${app.sms.nexmo.api-secret:}")
    private String nexmoApiSecret;

    // Local Provider Configuration
    @Value("${app.sms.local.api-key:}")
    private String localApiKey;

    @Value("${app.sms.local.api-url:}")
    private String localApiUrl;

    @Value("${app.sms.sender:Halaq.ma}")
    private String sender;

    private final RestTemplate restTemplate = new RestTemplate();
    private boolean twilioInitialized = false;

    @Override
    public void sendSms(String phoneNumber, String message) {
        // Valide et formate le numéro


        phoneNumber = formatPhoneNumber(phoneNumber);

        log.info("📱 Envoi SMS via {} vers {}", smsProvider, phoneNumber);

        switch (smsProvider.toLowerCase()) {
            case "twilio":
                sendViaTwilio(phoneNumber, message);
                break;
            case "nexmo":
            case "vonage":
                sendViaNexmo(phoneNumber, message);
                break;
            case "local":
                sendViaLocalProvider(phoneNumber, message);
                break;
            case "mock":
            default:
                sendViaMock(phoneNumber, message);
                break;
        }
    }

    @Override
    public void sendViaTwilio(String phoneNumber, String message) {
        try {
            // Initialise Twilio une seule fois
            if (!twilioInitialized) {
                if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty()) {
                    log.error("❌ Configuration Twilio manquante");
                    throw new IllegalStateException("Configuration Twilio incomplète");
                }
                Twilio.init(twilioAccountSid, twilioAuthToken);
                twilioInitialized = true;
                log.info("✅ Twilio initialisé");
            }

            if (twilioPhoneNumber.isEmpty()) {
                log.error("❌ Numéro Twilio manquant");
                throw new IllegalStateException("Numéro d'envoi Twilio non configuré");
            }

            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();

            log.info("✅ SMS Twilio envoyé - SID: {} - Status: {}",
                    twilioMessage.getSid(), twilioMessage.getStatus());

        } catch (Exception e) {
            log.error("❌ Erreur Twilio: {}", e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi SMS via Twilio: " + e.getMessage());
        }
    }

    @Override
    public void sendViaNexmo(String phoneNumber, String message) {
        try {
            if (nexmoApiKey.isEmpty() || nexmoApiSecret.isEmpty()) {
                log.error("❌ Configuration Nexmo manquante");
                throw new IllegalStateException("Configuration Nexmo incomplète");
            }

            String url = "https://rest.nexmo.com/sms/json";

            Map<String, String> payload = new HashMap<>();
            payload.put("from", sender);
            payload.put("to", phoneNumber);
            payload.put("text", message);
            payload.put("api_key", nexmoApiKey);
            payload.put("api_secret", nexmoApiSecret);
            payload.put("type", "unicode"); // Support caractères arabes

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ SMS Nexmo envoyé vers {}", phoneNumber);
            } else {
                log.error("❌ Erreur Nexmo - Status: {}", response.getStatusCode());
                throw new RuntimeException("Échec Nexmo: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Erreur Nexmo: {}", e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi SMS via Nexmo: " + e.getMessage());
        }
    }

    @Override
    public void sendViaLocalProvider(String phoneNumber, String message) {
        try {
            if (localApiUrl.isEmpty() || localApiKey.isEmpty()) {
                log.error("❌ Configuration provider local manquante");
                throw new IllegalStateException("Configuration provider local incomplète");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("phone", phoneNumber);
            payload.put("message", message);
            payload.put("sender", sender);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(localApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    localApiUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ SMS local provider envoyé vers {}", phoneNumber);
            } else {
                log.error("❌ Erreur provider local - Status: {}", response.getStatusCode());
                throw new RuntimeException("Échec provider local: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Erreur provider local: {}", e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi SMS via provider local: " + e.getMessage());
        }
    }

    @Override
    public void sendViaMock(String phoneNumber, String message) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📱 MOCK SMS");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("Provider: MOCK (Mode Test)");
        log.info("Destinataire: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("Expéditeur: {}", sender);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Simule un délai réseau
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isValidMoroccanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        String normalized = phoneNumber.replaceAll("\\s+", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "");

        // Formats acceptés:
        // +212612345678
        // 212612345678
        // 0612345678
        // 06 12 34 56 78
        boolean isValid = normalized.matches("^(\\+212|212|0)[5-7][0-9]{8}$");

        if (!isValid) {
            log.warn("⚠️ Numéro invalide: {} (normalisé: {})", phoneNumber, normalized);
        }

        return isValid;
    }

    @Override
    public String formatPhoneNumber(String phoneNumber) {
        // Nettoie le numéro
        String cleaned = phoneNumber.replaceAll("\\s+", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "");

        // Convertit au format international +212
        if (cleaned.startsWith("00212")) {
            cleaned = "+" + cleaned.substring(2);
        } else if (cleaned.startsWith("212") && !cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        } else if (cleaned.startsWith("0")) {
            cleaned = "+212" + cleaned.substring(1);
        } else if (!cleaned.startsWith("+")) {
            cleaned = "+212" + cleaned;
        }

        log.debug("📱 Numéro formaté: {} -> {}", phoneNumber, cleaned);
        return cleaned;
    }
}