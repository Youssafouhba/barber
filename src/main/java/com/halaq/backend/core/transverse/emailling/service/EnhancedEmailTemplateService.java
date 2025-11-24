package com.halaq.backend.core.transverse.emailling.service;

import com.halaq.backend.core.transverse.emailling.model.EmailTemplate;
import com.halaq.backend.core.transverse.emailling.model.ProcessedEmailTemplate;
import com.halaq.backend.core.transverse.emailling.repository.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class EnhancedEmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private EmailTextService emailTextService;

    private final TemplateEngine templateEngine;

    public EnhancedEmailTemplateService() {
        this.templateEngine = createStringTemplateEngine();
    }

    /**
     * Traite un template avec variables et textes dynamiques
     */
    public ProcessedEmailTemplate processTemplate(String templateName, Map<String, Object> variables, String language) {
        EmailTemplate template = emailTemplateRepository.findByNameAndIsActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException("Template non trouvé: " + templateName));

        // Créer le contexte avec les variables utilisateur
        Context context = new Context();
        context.setVariables(variables);

        // Ajouter les textes dynamiques au contexte
        Map<String, String> texts = emailTextService.getTextsByCategory(template.getCategory(), language);
        context.setVariable("texts", texts);

        // Traiter le sujet
        String processedSubject = templateEngine.process(template.getSubject(), context);

        // Traiter le contenu
        String processedContent = templateEngine.process(template.getContent(), context);

        return new ProcessedEmailTemplate(
                template.getName(),
                processedSubject,
                processedContent,
                template.getContentType()
        );
    }


    private TemplateEngine createStringTemplateEngine() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

        return engine;
    }
}
