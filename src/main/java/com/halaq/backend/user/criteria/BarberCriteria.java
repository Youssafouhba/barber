package com.halaq.backend.user.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.security.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ CORRECTED: BarberCriteria with all filter fields
 * Maps to URL parameters in AdminBarberController
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarberCriteria extends BaseCriteria {

    // Search fields
    private String fullName;
    private String email;
    private String phone;
    private String globalSearchQuery;
    // Filter fields
    private AccountStatus status;
    private Boolean isAvailable;
    private Boolean enabled;

    // Experience range filters
    private Integer yearsOfExperienceMin;
    private Integer yearsOfExperienceMax;

    // Rating range filters
    private Double averageRatingMin;
    private Double averageRatingMax;

    // Verification fields
    private Boolean documentsVerified;
    private Boolean portfolioVerified;
    private Boolean serviceZoneSetup;
    private Boolean cinVerified;

    // Business fields
    private String businessName;
    private String specialization;

    // Pagination (inherited from BaseCriteria)
    // page, limit, etc.

    @Override
    public String toString() {
        return "BarberCriteria{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", isAvailable=" + isAvailable +
                ", enabled=" + enabled +
                ", yearsOfExperienceMin=" + yearsOfExperienceMin +
                ", yearsOfExperienceMax=" + yearsOfExperienceMax +
                ", averageRatingMin=" + averageRatingMin +
                ", averageRatingMax=" + averageRatingMax +
                ", documentsVerified=" + documentsVerified +
                ", portfolioVerified=" + portfolioVerified +
                ", serviceZoneSetup=" + serviceZoneSetup +
                ", cinVerified=" + cinVerified +
                ", businessName='" + businessName + '\'' +
                ", specialization='" + specialization + '\'' +
                '}';
    }
}