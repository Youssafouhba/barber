package com.halaq.backend.core.transverse.emailling.repository;

import com.halaq.backend.core.transverse.emailling.model.EmailTemplate;
import com.halaq.backend.core.transverse.emailling.model.EmailText;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateService {

    @Transactional(readOnly = true)
    Optional<EmailTemplate> findByName(String name);

    @Transactional(readOnly = true)
    List<EmailTemplate> findAllActive();

    @Transactional
    EmailTemplate save(EmailTemplate emailTemplate);

    @Transactional
    void deactivateTemplate(Long id);

    @Transactional
    void activateTemplate(Long id);

    @Transactional(readOnly = true)
    List<EmailTemplate> searchByName(String name);

    @Transactional
    EmailTemplate createDefaultTemplate(String name, String subject, String content,List<EmailText> textDefinitions);

    @Transactional(readOnly = true)
    long countActiveTemplates();

    @Transactional
    EmailTemplate duplicateTemplate(Long id, String newName);
}
