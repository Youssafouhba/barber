package com.halaq.backend.core.transverse.emailling.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send email with HTML template
     */
    @Async
    public boolean sendTemplatedEmail(String toEmail, String subject, String templateName, Map<String, Object> templateVariables) {
        try {
            // Nettoyer et valider l'email
            String cleanEmail = cleanEmail(toEmail);
            if (cleanEmail == null || cleanEmail.isEmpty()) {
                logger.error("Invalid email address provided: {}", toEmail);
                return false;
            }

            // Create Thymeleaf context
            Context context = new Context();
            context.setVariables(templateVariables);

            // Process the template
            String htmlContent = templateEngine.process("email/" + templateName, context);

            // Create and send email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("97ca79002@smtp-brevo.com");
            helper.setTo(cleanEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            javaMailSender.send(message);

            logger.info("Email sent successfully to: {}", cleanEmail);
            return true;

        } catch (MessagingException e) {
            logger.error("Failed to send email to: {} - Error: {}", toEmail, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending email to: {} - Error: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send verification code email
     */
    @Async
    public void sendVerificationEmail(String toEmail, String userName, String verificationCode) {
        Map<String, Object> variables = Map.of(
                "userName", userName != null ? userName : "User",
                "verificationCode", verificationCode,
                "expirationMinutes", 10
        );

        sendTemplatedEmail(toEmail, "Verify Your Email Address", "email-verification", variables);
    }

    /**
     * Send password reset email with code
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetCode) {
        // L'URL de base est toujours utile pour l'image du logo
        String baseUrl = "https://951134d0e334.ngrok-free.app"; // Idéalement, à charger depuis la configuration
        String logoUrl = baseUrl + "/images/large-logo.png"; // Adaptez si nécessaire

        int expirationMinutes = 10;
        String expirationTime = expirationMinutes + " minutes";

        // Préparation des variables pour le template
        Map<String, Object> variables = Map.of(
                "userName", userName != null ? userName : "Utilisateur",
                "resetCode", resetCode, // La variable la plus importante ici !
                "expirationTime", expirationTime,
                "logoUrl", logoUrl,
                "email", toEmail
        );

        // Le nom du template peut être mis à jour pour refléter son usage mobile
        sendTemplatedEmail(toEmail, "Votre code de réinitialisation", "password-reset", variables);
    }
    /**
     * Send welcome email
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName != null ? userName : "User",
                "userEmail", toEmail,
                "registrationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                "dashboardUrl", "https://yourapp.com/dashboard"
        );

        sendTemplatedEmail(toEmail, "Welcome to Our Platform! 🎉", "welcome-email", variables);
    }

    /**
     * Send notification email
     */
    @Async
    public void sendNotificationEmail(String toEmail, String userName, String notificationTitle, String notificationMessage) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "notificationTitle", notificationTitle,
                "notificationMessage", notificationMessage,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
        );

         sendTemplatedEmail(toEmail, notificationTitle, "notification", variables);
    }

    /**
     * Send simple text email (fallback)
     */
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            // Nettoyer et valider l'email
            String cleanEmail = cleanEmail(toEmail);
            if (cleanEmail == null || cleanEmail.isEmpty()) {
                logger.error("Invalid email address provided: {}", toEmail);
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(cleanEmail);
            helper.setSubject(subject);
            helper.setText(content, false); // false = plain text

            javaMailSender.send(message);

            logger.info("Simple email sent successfully to: {}", cleanEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send simple email to: {} - Error: {}", toEmail, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending simple email to: {} - Error: {}", toEmail, e.getMessage(), e);
        }
    }

    /**
     * Nettoyer l'adresse email des caractères de contrôle, espaces et JSON
     */
    private String cleanEmail(String email) {
        if (email == null) {
            return null;
        }

        // Supprimer les espaces blancs, retours à la ligne, tabulations
        String cleaned = email.trim()
                .replaceAll("\\s+", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll("\\t", "");

        // Supprimer les caractères JSON si présents
        cleaned = cleaned.replaceAll("[{}\"\\[\\]]", "");

        // Extraire l'email si format "toEmail:email@example.com"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length > 1) {
                cleaned = parts[1];
            }
        }

        // Valider le format email de base
        if (!cleaned.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            logger.warn("Email format invalid after cleaning: {}", cleaned);
            return null;
        }

        return cleaned;
    }

    /**
     * Valider l'adresse email
     */
    public boolean isValidEmail(String email) {
        String cleanEmail = cleanEmail(email);
        return cleanEmail != null && !cleanEmail.isEmpty();
    }
}