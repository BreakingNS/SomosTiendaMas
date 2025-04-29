package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private final IUsuarioRepository usuarioRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(IUsuarioRepository usuarioRepository, 
                                   IPasswordResetTokenRepository passwordResetTokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Solicitar recuperación de contraseña
    public void solicitarRecuperacionPassword(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            TokenResetPassword tokenReset = new TokenResetPassword();
            tokenReset.setToken(token);
            tokenReset.setFechaExpiracion(Instant.now().plus(15, ChronoUnit.MINUTES));
            tokenReset.setUsado(false);
            tokenReset.setUsuario(usuario);
            passwordResetTokenRepository.save(tokenReset);
            System.out.println("Token de recuperación: " + token); // reemplazar por envío real
        });

        // Siempre responder OK aunque no exista, por seguridad
    }
}