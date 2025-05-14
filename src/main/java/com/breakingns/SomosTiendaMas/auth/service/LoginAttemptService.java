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

    // * Verifica si un identificador (username o email) e IP están actualmente bloqueados.
     
    public boolean isBlocked(String identifier, String ip) {
        if (identifier == null || identifier.isBlank()) {
            return false; // No bloqueamos si no hay identificador válido
        }

        // Si el identificador es un email
        if (identifier.contains("@")) {
            return usuarioRepository.findByEmail(identifier)
                    .map(Usuario::getUsername)
                    .map(username -> isBlockedByUsername(username, ip))
                    .orElseGet(() -> isBlockedByUsername(null, ip)); // Email inexistente: bloqueo solo por IP
        } else {
            return isBlockedByUsername(identifier, ip);
        }
    }

    // * Verifica si un par usuario-IP o solo IP está bloqueado.
    
    private boolean isBlockedByUsername(String username, String ip) {
        Optional<LoginAttempt> attemptOpt = (username == null)
                ? loginAttemptRepository.findByUsernameIsNullAndIp(ip)
                : loginAttemptRepository.findByUsernameAndIp(username, ip);

        return attemptOpt
                .map(attempt -> {
                    LocalDateTime blockedUntil = attempt.getBlockedUntil();
                    return blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now(clock));
                })
                .orElse(false);
    }

    // * Elimina los intentos fallidos registrados después de un inicio de sesión exitoso.
     
    @Transactional
    public void loginSucceeded(String username, String ip) {
        loginAttemptRepository.findByUsernameAndIp(username, ip)
                .ifPresent(loginAttemptRepository::delete);
    }

    // * Registra un intento fallido y bloquea si se supera el umbral.

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

    /**
     * Elimina intentos de login viejos cada hora.
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    public void eliminarIntentosExpirados() {
        LocalDateTime hace1Dia = LocalDateTime.now(clock).minusDays(1);
        int eliminados = loginAttemptRepository.deleteByLastAttemptAntesDe(hace1Dia);
        if (eliminados > 0) {
            log.info("Se eliminaron {} intentos fallidos viejos", eliminados);
        }
    }
}