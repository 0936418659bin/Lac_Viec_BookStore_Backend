package com.example.demo.service;

import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.TokenRefreshException;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;
    
    /**
     * Find a refresh token by token string
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token is not in database!"));
    }
    
    /**
     * Create a new refresh token for the given user
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Delete existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);
        
        // Create and save new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Verify if the refresh token is expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setExpired(true);
            refreshTokenRepository.save(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
    
    /**
     * Delete a refresh token
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshTokenRepository::delete);
    }
    
    /**
     * Clean up expired refresh tokens (runs every 24 hours)
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
    
    /**
     * Revoke a refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}
