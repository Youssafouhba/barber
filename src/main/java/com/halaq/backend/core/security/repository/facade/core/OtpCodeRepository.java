package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.security.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    /**
     * Trouve le dernier OTP non utilisé pour un numéro de téléphone
     */
    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.used = false ORDER BY o.createdAt DESC")
    Optional<OtpCode> findByPhoneAndUsedFalse(@Param("phone") String phone);

    /**
     * Trouve le dernier OTP pour un numéro de téléphone et une action spécifique
     */
    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.action = :action AND o.used = false ORDER BY o.createdAt DESC")
    Optional<OtpCode> findByPhoneAndActionAndUsedFalse(@Param("phone") String phone, @Param("action") String action);

    /**
     * Invalide tous les OTP existants pour un numéro de téléphone
     */
    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true, o.usedAt = CURRENT_TIMESTAMP WHERE o.phone = :phone AND o.used = false")
    void invalidateByPhone(@Param("phone") String phone);

    /**
     * Supprime les OTP expirés
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :currentTime")
    void deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Supprime les OTP utilisés plus anciens que la date spécifiée
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.used = true AND o.usedAt < :cutoffTime")
    void deleteOldUsedOtps(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Compte le nombre d'OTP envoyés pour un numéro dans une période donnée
     */
    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.phone = :phone AND o.createdAt > :since")
    long countByPhoneAndCreatedAtAfter(@Param("phone") String phone, @Param("since") LocalDateTime since);

    /**
     * Vérifie si un numéro de téléphone a atteint la limite d'envoi d'OTP
     */
    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.phone = :phone AND o.createdAt > :since")
    long countOtpAttemptsInPeriod(@Param("phone") String phone, @Param("since") LocalDateTime since);

    /**
     * Trouve tous les OTP non expirés pour un numéro de téléphone
     */
    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.expiresAt > CURRENT_TIMESTAMP")
    java.util.List<OtpCode> findActiveOtpsByPhone(@Param("phone") String phone);
}
