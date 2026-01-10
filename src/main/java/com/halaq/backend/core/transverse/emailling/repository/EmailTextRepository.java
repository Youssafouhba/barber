package com.halaq.backend.core.transverse.emailling.repository;


import com.halaq.backend.core.transverse.emailling.model.EmailText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface EmailTextRepository extends JpaRepository<EmailText, Long> {

    Optional<EmailText> findByTextKeyAndLanguageAndIsActiveTrue(String textKey, String language);

    List<EmailText> findByCategoryAndLanguageAndIsActiveTrue(String category, String language);

    @Query("SELECT et.textKey as textKey, et.textValue as value FROM EmailText et " +
            "WHERE et.language = ?1 AND et.category = ?2 AND et.isActive = true")
    List<Map<String, String>> findTextMapByCategoryAndLanguage(String language, String category);
}

