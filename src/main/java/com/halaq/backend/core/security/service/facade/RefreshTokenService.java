package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.controller.dto.response.RotatedTokenInfo;
import com.halaq.backend.core.security.entity.RefreshToken;
import com.halaq.backend.core.security.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);

    void deleteExpiredTokens();

    RotatedTokenInfo rotateRefreshToken(String oldToken);
}
