package com.halaq.backend.user.specification;

import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ✅ CORRECTED: BarberSpecification with all filter predicates
 * Properly handles null values and constructs JPA predicates
 */
@Slf4j
public class BarberSpecification extends AbstractSpecification<BarberCriteria, Barber> {

    public BarberSpecification(BarberCriteria criteria) {
        super(criteria);
    }

    @Override
    public Predicate toPredicate(Root<Barber> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (StringUtils.hasText(criteria.getGlobalSearchQuery())) {
            log.debug("[BarberSpecification] Mode RECHERCHE GLOBALE (OR) activé pour : {}", criteria.getGlobalSearchQuery());

            // Ajoute les wildcards (%) pour la recherche LIKE
            String searchQuery = "%" + criteria.getGlobalSearchQuery().toLowerCase() + "%";

            Predicate fullNamePredicate = builder.like(builder.lower(root.get("fullName")), searchQuery);
            Predicate emailPredicate = builder.like(builder.lower(root.get("email")), searchQuery);
            return builder.or(fullNamePredicate, emailPredicate);
        }
        log.debug("[BarberSpecification] Mode FILTRAGE AVANCÉ (AND) activé.");
        List<Predicate> predicates = new ArrayList<>();
        // ============================================
        // SEARCH FIELDS (LIKE - partial matching)
        // ============================================

        if (criteria.getFullName() != null && !criteria.getFullName().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding fullName filter: {}", criteria.getFullName());
            addPredicate("fullName", criteria.getFullName(), Operator.LIKE.toString());
        }

        if (criteria.getEmail() != null && !criteria.getEmail().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding email filter: {}", criteria.getEmail());
            addPredicate("email", criteria.getEmail(), Operator.LIKE.toString());
        }

        if (criteria.getPhone() != null && !criteria.getPhone().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding phone filter: {}", criteria.getPhone());
            addPredicate("phone", criteria.getPhone(), Operator.LIKE.toString());
        }

        // ============================================
        // STATUS FILTERS (EQUALS)
        // ============================================
        if (criteria.getStatus() != null) {
            log.debug("[BarberSpecification] Adding status filter: {}", criteria.getStatus());
            addPredicate("status", criteria.getStatus().toString(), Operator.EQUALS.toString());
        }

        // ============================================
        // BOOLEAN FILTERS
        // ============================================
        if (criteria.getIsAvailable() != null) {
            log.debug("[BarberSpecification] Adding isAvailable filter: {}", criteria.getIsAvailable());
            addPredicate("isAvailable", criteria.getIsAvailable(), Operator.EQUALS);
        }

        if (criteria.getEnabled() != null) {
            log.debug("[BarberSpecification] Adding enabled filter: {}", criteria.getEnabled());
            addPredicate("enabled", criteria.getEnabled(), Operator.EQUALS);
        }

        // ============================================
        // EXPERIENCE RANGE FILTERS
        // ============================================
        if (criteria.getYearsOfExperienceMin() != null) {
            log.debug("[BarberSpecification] Adding yearsOfExperienceMin filter: {}", criteria.getYearsOfExperienceMin());
            addPredicate("yearsOfExperience",
                    criteria.getYearsOfExperienceMin().toString(),
                    Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }

        if (criteria.getYearsOfExperienceMax() != null) {
            log.debug("[BarberSpecification] Adding yearsOfExperienceMax filter: {}", criteria.getYearsOfExperienceMax());
            addPredicate("yearsOfExperience",
                    criteria.getYearsOfExperienceMax().toString(),
                    Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }

        // ============================================
        // RATING RANGE FILTERS
        // ============================================
        if (criteria.getAverageRatingMin() != null) {
            log.debug("[BarberSpecification] Adding averageRatingMin filter: {}", criteria.getAverageRatingMin());
            addPredicate("averageRating",
                    criteria.getAverageRatingMin().toString(),
                    Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }

        if (criteria.getAverageRatingMax() != null) {
            log.debug("[BarberSpecification] Adding averageRatingMax filter: {}", criteria.getAverageRatingMax());
            addPredicate("averageRating",
                    criteria.getAverageRatingMax().toString(),
                    Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }

        // ============================================
        // VERIFICATION STATUS FILTERS
        // ============================================
        if (criteria.getDocumentsVerified() != null) {
            log.debug("[BarberSpecification] Adding documentsVerified filter: {}", criteria.getDocumentsVerified());
            addPredicate("documentsVerified",
                    criteria.getDocumentsVerified(),
                    Operator.EQUALS);
        }

        if (criteria.getPortfolioVerified() != null) {
            log.debug("[BarberSpecification] Adding portfolioVerified filter: {}", criteria.getPortfolioVerified());
            addPredicate("portfolioVerified",
                    criteria.getPortfolioVerified(),
                    Operator.EQUALS);
        }

        if (criteria.getServiceZoneSetup() != null) {
            log.debug("[BarberSpecification] Adding serviceZoneSetup filter: {}", criteria.getServiceZoneSetup());
            addPredicate("serviceZoneSetup",
                    criteria.getServiceZoneSetup(),
                    Operator.EQUALS);
        }

        if (criteria.getCinVerified() != null) {
            log.debug("[BarberSpecification] Adding cinVerified filter: {}", criteria.getCinVerified());
            addPredicate("cinVerified",
                    criteria.getCinVerified(),
                    Operator.EQUALS);
        }

        // ============================================
        // BUSINESS FILTERS
        // ============================================
        if (criteria.getBusinessName() != null && !criteria.getBusinessName().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding businessName filter: {}", criteria.getBusinessName());
            addPredicate("businessName",
                    criteria.getBusinessName(),
                    Operator.LIKE.toString());
        }

        if (criteria.getSpecialization() != null && !criteria.getSpecialization().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding specialization filter: {}", criteria.getSpecialization());
            addPredicate("specialization",
                    criteria.getSpecialization(),
                    Operator.LIKE.toString());
        }
        log.debug("[BarberSpecification] Constructed {} predicates", predicates.size());
        return builder.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * ✅ FIXED: Construct predicates from criteria fields
     * Now handles all filter fields with null safety
     */
    @Override
    public void constructPredicates() {
        log.debug("[BarberSpecification] Mode FILTRAGE AVANCÉ (AND) activé.");
        List<Predicate> predicates = new ArrayList<>();
        // ============================================
        // SEARCH FIELDS (LIKE - partial matching)
        // ============================================

        if (criteria.getFullName() != null && !criteria.getFullName().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding fullName filter: {}", criteria.getFullName());
            addPredicate("fullName", criteria.getFullName(), Operator.LIKE.toString());
        }

        if (criteria.getEmail() != null && !criteria.getEmail().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding email filter: {}", criteria.getEmail());
            addPredicate("email", criteria.getEmail(), Operator.LIKE.toString());
        }

        if (criteria.getPhone() != null && !criteria.getPhone().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding phone filter: {}", criteria.getPhone());
            addPredicate("phone", criteria.getPhone(), Operator.LIKE.toString());
        }

        // ============================================
        // STATUS FILTERS (EQUALS)
        // ============================================
        if (criteria.getStatus() != null) {
            log.debug("[BarberSpecification] Adding status filter: {}", criteria.getStatus());
            addPredicate("status", criteria.getStatus().toString(), Operator.EQUALS.toString());
        }

        // ============================================
        // BOOLEAN FILTERS
        // ============================================
        if (criteria.getIsAvailable() != null) {
            log.debug("[BarberSpecification] Adding isAvailable filter: {}", criteria.getIsAvailable());
            addPredicate("isAvailable", criteria.getIsAvailable(), Operator.EQUALS);
        }

        if (criteria.getEnabled() != null) {
            log.debug("[BarberSpecification] Adding enabled filter: {}", criteria.getEnabled());
            addPredicate("enabled", criteria.getEnabled(), Operator.EQUALS);
        }

        // ============================================
        // EXPERIENCE RANGE FILTERS
        // ============================================
        if (criteria.getYearsOfExperienceMin() != null) {
            log.debug("[BarberSpecification] Adding yearsOfExperienceMin filter: {}", criteria.getYearsOfExperienceMin());
            addPredicate("yearsOfExperience",
                    criteria.getYearsOfExperienceMin().toString(),
                    Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }

        if (criteria.getYearsOfExperienceMax() != null) {
            log.debug("[BarberSpecification] Adding yearsOfExperienceMax filter: {}", criteria.getYearsOfExperienceMax());
            addPredicate("yearsOfExperience",
                    criteria.getYearsOfExperienceMax().toString(),
                    Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }

        // ============================================
        // RATING RANGE FILTERS
        // ============================================
        if (criteria.getAverageRatingMin() != null) {
            log.debug("[BarberSpecification] Adding averageRatingMin filter: {}", criteria.getAverageRatingMin());
            addPredicate("averageRating",
                    criteria.getAverageRatingMin().toString(),
                    Operator.GREATER_THAN_OR_EQUAL_TO.toString());
        }

        if (criteria.getAverageRatingMax() != null) {
            log.debug("[BarberSpecification] Adding averageRatingMax filter: {}", criteria.getAverageRatingMax());
            addPredicate("averageRating",
                    criteria.getAverageRatingMax().toString(),
                    Operator.LESS_THAN_OR_EQUAL_TO.toString());
        }

        // ============================================
        // VERIFICATION STATUS FILTERS
        // ============================================
        if (criteria.getDocumentsVerified() != null) {
            log.debug("[BarberSpecification] Adding documentsVerified filter: {}", criteria.getDocumentsVerified());
            addPredicate("documentsVerified",
                    criteria.getDocumentsVerified(),
                    Operator.EQUALS);
        }

        if (criteria.getPortfolioVerified() != null) {
            log.debug("[BarberSpecification] Adding portfolioVerified filter: {}", criteria.getPortfolioVerified());
            addPredicate("portfolioVerified",
                    criteria.getPortfolioVerified(),
                    Operator.EQUALS);
        }

        if (criteria.getServiceZoneSetup() != null) {
            log.debug("[BarberSpecification] Adding serviceZoneSetup filter: {}", criteria.getServiceZoneSetup());
            addPredicate("serviceZoneSetup",
                    criteria.getServiceZoneSetup(),
                    Operator.EQUALS);
        }

        if (criteria.getCinVerified() != null) {
            log.debug("[BarberSpecification] Adding cinVerified filter: {}", criteria.getCinVerified());
            addPredicate("cinVerified",
                    criteria.getCinVerified(),
                    Operator.EQUALS);
        }

        // ============================================
        // BUSINESS FILTERS
        // ============================================
        if (criteria.getBusinessName() != null && !criteria.getBusinessName().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding businessName filter: {}", criteria.getBusinessName());
            addPredicate("businessName",
                    criteria.getBusinessName(),
                    Operator.LIKE.toString());
        }

        if (criteria.getSpecialization() != null && !criteria.getSpecialization().trim().isEmpty()) {
            log.debug("[BarberSpecification] Adding specialization filter: {}", criteria.getSpecialization());
            addPredicate("specialization",
                    criteria.getSpecialization(),
                    Operator.LIKE.toString());
        }

        log.debug("[BarberSpecification] Constructed {} predicates", predicates.size());
    }
}