package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.PasswordIgualAAnteriorException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordIncorrectaException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordInvalidaException;
import com.breakingns.SomosTiendaMas.security.exception.TokenExpiradoException;
import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.TokenYaUsadoException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Buscando usuario con email: {}", email);
        
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            log.info("Mail encontrado: {}", email);
            
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

    public void changePassword(Usuario usuario, String currentPassword, String newPassword) {
        // Verificación de contraseña actual
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new PasswordIncorrectaException("La contraseña actual es incorrecta.");
        }

        // Verificación de que la nueva contraseña no sea igual a la actual
        if (passwordEncoder.matches(newPassword, usuario.getPassword())) {
            throw new PasswordIgualAAnteriorException("La nueva contraseña no puede ser igual a la actual.");
        }

        // Validación de la contraseña nueva (por ejemplo, longitud mínima)
        if (newPassword.length() < 6) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres.");
        }
        
        // Validación de la contraseña nueva (por ejemplo, longitud maxima)
        if (newPassword.length() > 16) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.");
        }

        // Si pasa todas las validaciones, actualizamos la contraseña
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }
    
    public void resetearPassword(String token, String nuevaPassword) {
        log.info("Por resetear password con token: {}", token);

        Optional<TokenResetPassword> optional = passwordResetTokenRepository.findByToken(token);

        if (optional.isEmpty()) {
            throw new TokenNoEncontradoException("El token no existe.");
        }

        TokenResetPassword tokenEntity = optional.get();

        // Validación si el token no expiro
        if (tokenEntity.isExpirado()) {
            throw new TokenExpiradoException("El token expiró.");
        }
        
        // Validación si el token fue usado
        if (tokenEntity.isUsado()) {
            throw new TokenYaUsadoException("El token ya fue usado.");
        }
        /*
        // Validación de la contraseña nueva (por ejemplo, longitud mínima)
        if (nuevaPassword.length() < 6) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres.");
        }
        
        // Validación de la contraseña nueva (por ejemplo, longitud maxima)
        if (nuevaPassword.length() > 16) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.");
        }
        */
        Usuario usuario = tokenEntity.getUsuario();

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        tokenEntity.setUsado(true);
        passwordResetTokenRepository.save(tokenEntity);
    }

    void solicitarRecuperacionPasswordExpirado(String email) { //SOLO PRUEBAS
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            TokenResetPassword tokenReset = new TokenResetPassword();
            tokenReset.setToken(token);
            tokenReset.setFechaExpiracion(Instant.now());
            tokenReset.setUsado(false);
            tokenReset.setUsuario(usuario);
            passwordResetTokenRepository.save(tokenReset);
            System.out.println("Token de recuperación: " + token); // reemplazar por envío real
        });
    }

    void solicitarRecuperacionPasswordUsado(String email) { //SOLO PRUEBAS
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            TokenResetPassword tokenReset = new TokenResetPassword();
            tokenReset.setToken(token);
            tokenReset.setFechaExpiracion((Instant.now().plus(15, ChronoUnit.MINUTES)));
            tokenReset.setUsado(true);
            tokenReset.setUsuario(usuario);
            passwordResetTokenRepository.save(tokenReset);
            System.out.println("Token de recuperación: " + token); // reemplazar por envío real
        });
    }
}