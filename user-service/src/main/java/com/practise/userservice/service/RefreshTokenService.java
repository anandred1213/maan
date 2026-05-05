package com.practise.userservice.service;

import com.practise.userservice.entity.RefreshToken;
import com.practise.userservice.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String userEmail) {
        log.info("Creating refresh token for user: {}", userEmail);
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserEmail(userEmail);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));
        
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created successfully for user: {}", userEmail);
        return saved;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.warn("Refresh token expired for user: {}", token.getUserEmail());
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again");
        }
        return token;
    }

    @Transactional
    public void deleteByUserEmail(String userEmail) {
        log.info("Deleting refresh tokens for user: {}", userEmail);
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }
}
