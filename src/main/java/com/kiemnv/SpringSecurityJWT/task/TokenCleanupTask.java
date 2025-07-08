package com.kiemnv.SpringSecurityJWT.task;

import com.kiemnv.SpringSecurityJWT.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            refreshTokenRepository.cleanupExpiredTokens();
            log.info("Completed cleanup of expired refresh tokens");
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }
}
