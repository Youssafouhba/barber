package com.halaq.backend.core.transverse.emailling.service;


import com.halaq.backend.core.transverse.emailling.model.EmailTemplate;
import com.halaq.backend.core.transverse.emailling.model.EmailText;
import com.halaq.backend.core.transverse.emailling.repository.EmailTemplateRepository;
import com.halaq.backend.core.transverse.emailling.repository.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    @Autowired
    private EmailTemplateRepository dao;

    @Autowired
    private EmailTextService emailTextService;

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailTemplate> findByName(String name) {
        return dao.findByNameAndIsActiveTrue(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplate> findAllActive() {
        return dao.findByIsActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional
    public EmailTemplate save(EmailTemplate emailTemplate) {
        // Vérifier l'unicité du nom
        if (emailTemplate.getId() == null) {
            if (dao.findByNameAndIsActiveTrue(emailTemplate.getName()).isPresent()) {
                throw new RuntimeException("Un template avec ce nom existe déjà");
            }
        } else {
            if (dao.existsByNameAndIdNot(emailTemplate.getName(), emailTemplate.getId())) {
                throw new RuntimeException("Un template avec ce nom existe déjà");
            }
        }

        return dao.save(emailTemplate);
    }

    @Override
    @Transactional
    public void deactivateTemplate(Long id) {
        EmailTemplate template = dao.findEmailTemplateById(id);
        if (template != null) {
            template.setIsActive(false);
            save(template);
        }
    }

    @Override
    @Transactional
    public void activateTemplate(Long id) {
        EmailTemplate template = dao.findEmailTemplateById(id);
        if (template != null) {
            template.setIsActive(true);
            save(template);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplate> searchByName(String name) {
        return dao.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional
    public EmailTemplate createDefaultTemplate(String name, String subject, String content, List<EmailText> textDefinitions) {
        Optional<EmailTemplate> existing = findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }

        EmailTemplate template = new EmailTemplate();
        template.setName(name);
        template.setSubject(subject);
        template.setContent(content);
        template.setDescription("Template par défaut pour " + name);
        template.setIsActive(true);
        EmailTemplate template1 = save(template);

        // Sauvegarde les textes dynamiques associés
        if (textDefinitions != null) {
            for (EmailText definition : textDefinitions) {
                emailTextService.saveText(definition.getTextKey(), definition.getTextValue(), definition.getLanguage(), definition.getCategory());
            }
        }
        return template1;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveTemplates() {
        return dao.countActiveTemplates();
    }

    @Override
    @Transactional
    public EmailTemplate duplicateTemplate(Long id, String newName) {
        EmailTemplate original =dao.findEmailTemplateById(id);
        if (original == null) {
            throw new RuntimeException("Template non trouvé");
        }

        EmailTemplate duplicate = new EmailTemplate();
        duplicate.setName(newName);
        duplicate.setSubject(original.getSubject());
        duplicate.setContent(original.getContent());
        duplicate.setContentType(original.getContentType());
        duplicate.setDescription("Copie de " + original.getName());
        duplicate.setIsActive(true);

        return save(duplicate);
    }

    public List<EmailTemplate> findAll() {
        return dao.findAll();
    }
}