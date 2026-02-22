package com.clinic.util;

import com.clinic.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "0 0 2 * * *")  // Daily at 2 AM
    public void cleanupExpiredTokens() {
        log.info("Running token blacklist cleanup...");
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Token cleanup complete");
    }
}