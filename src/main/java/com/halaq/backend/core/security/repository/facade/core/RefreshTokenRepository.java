package com.halaq.backend.core.security.repository.facade.core;

import com.halaq.backend.core.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUserId(Long userId);

    @Modifying
    void deleteByExpiryDateBefore(LocalDateTime expiry);

    List<RefreshToken> findByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.userId = :userId AND r.expiryDate > CURRENT_TIMESTAMP")
    long countActiveTokensByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId AND r.createdAt < :cutoffDate")
    void deleteOldTokensForUser(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);
}