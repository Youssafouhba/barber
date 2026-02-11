package com.halaq.backend.core.security.service.impl;

import com.halaq.backend.core.security.controller.dto.response.RotatedTokenInfo;
import com.halaq.backend.core.security.entity.RefreshToken;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import com.halaq.backend.core.security.repository.facade.core.RefreshTokenRepository;
import com.halaq.backend.core.security.service.facade.RefreshTokenService;
import com.halaq.backend.core.security.service.facade.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserService userService;

    @Value("${app.jwt.refresh-expiration-days:30}")
    private int refreshTokenExpirationDays;

    private final JwtTokenUtil jwtTokenUtil;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        // 1. Génére un nouveau token
        String refreshTokenString =  jwtTokenUtil.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .createdAt(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    @Override
    public RotatedTokenInfo rotateRefreshToken(String oldToken) {
        // 1. Trouve, valide l'expiration et récupère l'ancien token en une seule étape
        RefreshToken token = findByToken(oldToken)
                .map(this::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Refresh token not found or expired"));

        // 2. Récupère l'utilisateur associé
        User user = userService.findById(token.getUserId());
        if (user == null) {
            throw new RuntimeException("User associated with refresh token not found");
        }

        // 3. Supprime l'ancien token
        refreshTokenRepository.delete(token);

        // 4. Crée un nouveau token
        RefreshToken newRefreshToken = createRefreshToken(user);

        // 5. Retourne le nouveau token et l'utilisateur
        return new RotatedTokenInfo(newRefreshToken.getToken(), user);
    }
}