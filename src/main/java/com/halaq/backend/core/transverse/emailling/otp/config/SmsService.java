package com.halaq.backend.core.transverse.emailling.otp.config;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    private final TwilioConfig twilioConfig;
    @Value("${twilio.trial-mode:true}")
    private boolean trialMode;

    public boolean sendSms(String toPhoneNumber, String messageBody) {
        try {
            // En mode Trial, ajoutez un préfixe au message
            if (trialMode) {
                messageBody = "[TRIAL MODE] " + messageBody;
            }

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioConfig.getPhoneNumber()), // Doit être un numéro Twilio
                    messageBody
            ).create();

            log.info("SMS envoyé avec succès. SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS: {}", e.getMessage());
            return false;
        }
    }
}