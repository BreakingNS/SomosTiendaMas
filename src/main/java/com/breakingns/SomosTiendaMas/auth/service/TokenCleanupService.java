package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenCleanupService {

    private final IPasswordResetTokenRepository tokenRepo;

    public TokenCleanupService(IPasswordResetTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    @Scheduled(cron = "0 0 3 * * ?") // todos los días a las 3 AM
    public void eliminarTokensExpirados() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        tokenRepo.deleteAllByFechaExpiracionBeforeAndUsadoTrue(cutoff);
    }
}