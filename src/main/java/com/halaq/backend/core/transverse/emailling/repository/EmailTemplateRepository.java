package com.halaq.backend.core.transverse.emailling.repository;


import com.halaq.backend.core.transverse.emailling.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long>, JpaSpecificationExecutor<EmailTemplate> {

    /**
     * Trouve un template par son nom
     */
    Optional<EmailTemplate> findByNameAndIsActiveTrue(String name);

    /**
     * Trouve tous les templates actifs
     */
    List<EmailTemplate> findByIsActiveTrueOrderByNameAsc();

    /**
     * Vérifie si un template avec ce nom existe déjà
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Trouve tous les templates par nom (case insensitive)
     */
    @Query("SELECT e FROM EmailTemplate e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.isActive = true")
    List<EmailTemplate> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Compte le nombre de templates actifs
     */
    @Query("SELECT COUNT(e) FROM EmailTemplate e WHERE e.isActive = true")
    long countActiveTemplates();

    EmailTemplate findEmailTemplateById(Long id);
}