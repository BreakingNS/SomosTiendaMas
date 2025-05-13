package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.LoginAttempt;
import com.breakingns.SomosTiendaMas.auth.repository.LoginAttemptRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BLOCK_MINUTES = 1;
    
    private final Clock clock;
    
    private final LoginAttemptRepository loginAttemptRepository;
    private final IUsuarioRepository usuarioRepository;

    @Autowired
    public LoginAttemptService(Clock clock, 
            LoginAttemptRepository loginAttemptRepository,
            IUsuarioRepository usuarioRepository) {
        this.clock = clock;
        this.loginAttemptRepository = loginAttemptRepository;
        this.usuarioRepository = usuarioRepository;
    }
    /*
    public boolean isBlocked(String email, String ip) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIp(email, ip);

        return attemptOpt
                .map(attempt -> attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(LocalDateTime.now()))
                .orElse(false);
    }*/
    
    public boolean isBlocked(String identifier, String ip) {
        // Si el identificador es un email...
        if (identifier.contains("@")) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(identifier);
            if (usuarioOpt.isPresent()) {
                String username = usuarioOpt.get().getUsername();
                return isBlockedByUsername(username, ip);
            } else {
                // Intentar bloquear solo por IP si el email no existe
                return isBlockedByUsername(null, ip); // Usa null como username
            }
        } else {
            return isBlockedByUsername(identifier, ip);
        }
    }

    private boolean isBlockedByUsername(String username, String ip) {
        Optional<LoginAttempt> attemptOpt;
        if (username == null) {
            attemptOpt = loginAttemptRepository.findByUsernameIsNullAndIp(ip);
        } else {
            attemptOpt = loginAttemptRepository.findByUsernameAndIp(username, ip);
        }

        return attemptOpt
                .map(attempt -> attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void loginSucceeded(String username, String ip) {
        loginAttemptRepository.findByUsernameAndIp(username, ip).ifPresent(loginAttemptRepository::delete);
    }

    @Transactional
    public void loginFailed(String username, String ip) {
        LocalDateTime now = LocalDateTime.now(clock);

        LoginAttempt attempt = loginAttemptRepository
                .findByUsernameAndIp(username, ip)
                .orElseGet(() -> new LoginAttempt(username, ip, 0, now, null));

        int currentAttempts = attempt.getFailedAttempts() + 1;
        attempt.setFailedAttempts(currentAttempts);
        attempt.setLastAttempt(now);

        if (currentAttempts >= MAX_ATTEMPTS) {
            long blockMinutes = (long) (INITIAL_BLOCK_MINUTES * Math.pow(2, currentAttempts - MAX_ATTEMPTS));
            LocalDateTime blockedUntil = now.plusMinutes(blockMinutes);
            attempt.setBlockedUntil(blockedUntil);
            log.warn("Usuario [{}] con IP [{}] bloqueado hasta {}", username, ip, blockedUntil);
        }

        loginAttemptRepository.save(attempt);
    }
    
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    public void eliminarIntentosExpirados() {
        LocalDateTime hace1Dia = LocalDateTime.now().minusDays(1);
        int eliminados = loginAttemptRepository.deleteByLastAttemptAntesDe(hace1Dia);
        if (eliminados > 0) {
            log.info("Se eliminaron {} intentos fallidos viejos", eliminados);
        }
    }
    
}