package com.halaq.backend.core.transverse.emailling.service;

import com.halaq.backend.core.transverse.emailling.model.EmailText;
import com.halaq.backend.core.transverse.emailling.repository.EmailTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailTextService {

    @Autowired
    private EmailTextRepository emailTextRepository;

    /**
     * Récupère un texte par sa clé et langue
     */
    public String getText(String key, String language) {
        Optional<EmailText> emailText = emailTextRepository
                .findByTextKeyAndLanguageAndIsActiveTrue(key, language);

        return emailText.map(EmailText::getTextValue).orElse("{{" + key + "}}");
    }

    /**
     * Récupère un texte avec langue par défaut (français)
     */
    public String getText(String key) {
        return getText(key, "fr");
    }

    /**
     * Récupère tous les textes d'une catégorie pour une langue
     */
    public Map<String, String> getTextsByCategory(String category, String language) {
        List<EmailText> texts = emailTextRepository
                .findByCategoryAndLanguageAndIsActiveTrue(category, language);

        Map<String, String> textMap = new HashMap<>();
        for (EmailText text : texts) {
            textMap.put(text.getTextKey(), text.getTextValue());
        }

        return textMap;
    }

    /**
     * Sauvegarde ou met à jour un texte
     */
    public EmailText saveText(String key, String value, String language, String category) {
        Optional<EmailText> existing = emailTextRepository
                .findByTextKeyAndLanguageAndIsActiveTrue(key, language);

        EmailText emailText;
        if (existing.isPresent()) {
            emailText = existing.get();
            emailText.setTextValue(value);
        } else {
            emailText = new EmailText("next-validator-notification",key, value, category);
        }

        return emailTextRepository.save(emailText);
    }
}