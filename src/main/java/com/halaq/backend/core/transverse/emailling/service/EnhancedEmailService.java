package com.halaq.backend.core.transverse.emailling.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.halaq.backend.core.transverse.emailling.model.ProcessedEmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EnhancedEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EnhancedEmailTemplateService templateService;

    /**
     * Envoie un email avec support multilingue
     */
    public void sendEmailFromTemplate(String templateName, String to,
                                      Map<String, Object> variables, String language)
            throws MessagingException {

        ProcessedEmailTemplate processedTemplate = templateService
                .processTemplate(templateName, variables, language);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(processedTemplate.getSubject());
        helper.setText(processedTemplate.getContent(), true);

        mailSender.send(message);
    }

    /**
     * Version avec langue par défaut
     */
    public void sendEmailFromTemplate(String templateName, String to, Map<String, Object> variables)
            throws MessagingException {
        sendEmailFromTemplate(templateName, to, variables, "fr");
    }
}