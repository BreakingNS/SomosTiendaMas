package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.EmailNoVerificadoException;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PasswordResetService {

    private final IUsuarioRepository usuarioRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;

    public PasswordResetService(IUsuarioRepository usuarioRepository,
                                IPasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                RefreshTokenService refreshTokenService,
                                TokenEmitidoService tokenEmitidoService,
                                SesionActivaService sesionActivaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
    }
    
    public void solicitarRecuperacionPassword(String email) {
        log.info("Buscando usuario con email: {}", email);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            // No existe: silenciar, responder OK al cliente (evitar enumeración)
            log.debug("No existe usuario con email {}", email);
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // Si el email no está verificado, no enviar el correo (silencioso)
        if (!Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            log.info("El usuario existe pero el email no está verificado, no se envía el correo. email={}", email);
            return;
        }

        // Buscar token activo (no usado) para este usuario
        Optional<TokenResetPassword> existenteOpt =
                passwordResetTokenRepository.findByUsuario_IdUsuarioAndUsadoFalse(usuario.getIdUsuario());

        if (existenteOpt.isPresent()) {
            TokenResetPassword existente = existenteOpt.get();
            if (!existente.isExpirado()) {
                log.info("Reusando token de recuperación existente para usuario id={}", usuario.getIdUsuario());
                emailService.enviarEmailRecuperacionPassword(email, existente.getToken());
                return;
            } else {
                // Limpieza: eliminar token expirado (o marcarlo usado)
                try {
                    passwordResetTokenRepository.delete(existente);
                } catch (Exception e) {
                    log.warn("No se pudo eliminar token expirado para usuario id={}: {}", usuario.getIdUsuario(), e.getMessage());
                }
            }
        }

        // Crear nuevo token
        String token = TokenResetPassword.generarTokenAlfanumerico(32);
        TokenResetPassword tokenReset = new TokenResetPassword();
        tokenReset.setToken(token);
        tokenReset.setFechaExpiracion(Instant.now().plus(15, ChronoUnit.MINUTES));
        tokenReset.setUsado(false);
        tokenReset.setUsuario(usuario);
        passwordResetTokenRepository.save(tokenReset);

        log.debug("Token de recuperación generado para usuario id={}", usuario.getIdUsuario());

        // Enviar email con enlace de recuperación
        emailService.enviarEmailRecuperacionPassword(email, token);
        log.info("Token de recuperación generado y email enviado para email={}", email);
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
    
    @Transactional
    public void resetearPassword(String tokenValue, String nuevaPassword) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new IllegalArgumentException("Token inválido");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // Intentar marcar el token como usado de forma atómica.
        int updated = passwordResetTokenRepository.markAsUsedIfValid(tokenValue);
        if (updated == 0) {
            // determinar causa y lanzar excepción genérica de token inválido/expirado/ya usado
            Optional<TokenResetPassword> maybe = passwordResetTokenRepository.findByToken(tokenValue);
            if (maybe.isEmpty()) {
                throw new IllegalArgumentException("Token inválido o inexistente");
            }
            TokenResetPassword t = maybe.get();
            if (t.isExpirado()) throw new IllegalArgumentException("Token expirado");
            if (Boolean.TRUE.equals(t.isUsado())) throw new IllegalArgumentException("Token ya fue utilizado");
            throw new IllegalArgumentException("Token inválido");
        }

        // token ya marcado como usado en BD; obtener entidad para conocer usuario
        TokenResetPassword token = passwordResetTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new IllegalArgumentException("Token inválido o inexistente"));

        Usuario usuario = token.getUsuario();
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado para el token");
        }

        // Actualizar contraseña (hashear)
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // NOTA: token.usado ya fue puesto a true por la actualización condicional.
        // Ejecutar revocaciones (mantengo llamadas existentes)
        String username = usuario.getUsername();
        try {
            if (refreshTokenService != null) refreshTokenService.logoutTotal(username);
        } catch (Exception e) {
            log.warn("Error al revocar refresh tokens para usuario {}: {}", username, e.getMessage());
        }
        try {
            if (tokenEmitidoService != null) tokenEmitidoService.revocarTodosLosTokensActivos(username);
        } catch (Exception e) {
            log.warn("Error al revocar tokens emitidos para usuario {}: {}", username, e.getMessage());
        }
        try {
            if (sesionActivaService != null) sesionActivaService.revocarTodasLasSesiones(username);
        } catch (Exception e) {
            log.warn("Error al revocar sesiones activas para usuario {}: {}", username, e.getMessage());
        }

        log.info("Reset exitoso y revocaciones ejecutadas para usuario id={}, username={}", usuario.getIdUsuario(), username);
    }

    /* 
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        log.info("Reset password inicio token={}", token);

        // 1) bloquear y obtener la entidad del token
        TokenResetPassword tokenEntity = passwordResetTokenRepository.findByTokenForUpdate(token)
            .orElseThrow(() -> new TokenNoEncontradoException("Token no existe"));

        // 2) validar estado del token
        if (Boolean.TRUE.equals(tokenEntity.isUsado())) {
            throw new TokenYaUsadoException("Token ya fue usado");
        }
        if (tokenEntity.isExpirado()) {
            throw new TokenExpiradoException("Token expirado");
        }

        // 3) validar contraseña nueva (no marcar token todavía)
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new PasswordInvalidaException("La contraseña no cumple requisitos");
        }
        if (nuevaPassword.length() > 128) {
            throw new PasswordInvalidaException("La contraseña excede longitud máxima");
        }

        // 4) aplicar cambio y marcar token como usado en la misma transacción
        Usuario usuario = tokenEntity.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        tokenEntity.setUsado(true);
        // opcional: tokenEntity.setFechaUso(Instant.now());
        passwordResetTokenRepository.save(tokenEntity);

        // 5) REVOCACIONES: revocar refresh tokens, access tokens y sesiones del usuario ----
        // usar el username porque no tienes accessToken en este flujo
        String username = usuario.getUsername();

        // revoca todos los refresh tokens del usuario
        refreshTokenService.logoutTotal(username);

        // revoca todos los access tokens emitidos (o marcarlos como revocados)
        tokenEmitidoService.revocarTodosLosTokensActivos(username);

        // revoca/elimna sesiones activas
        sesionActivaService.revocarTodasLasSesiones(username);

        log.info("Reset exitoso para usuario id={}", usuario.getIdUsuario());

        
    }*/
    /* 
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        log.info("Por resetear password con token (atómico): {}", token);

        Instant ahora = Instant.now();

        // Intentar marcar como usado de forma atómica; devuelve 1 si se marcó, 0 si no (no existe/expirado/ya usado)
        int updated = passwordResetTokenRepository.markAsUsedIfValid(token, ahora);
        if (updated == 0) {
            // Determinar motivo para lanzar la excepción adecuada
            Optional<TokenResetPassword> maybe = passwordResetTokenRepository.findByToken(token);
            if (maybe.isEmpty()) {
                throw new TokenNoEncontradoException("El token no existe.");
            }
            TokenResetPassword te = maybe.get();
            if (te.isExpirado()) {
                throw new TokenExpiradoException("El token expiró.");
            }
            if (te.isUsado()) {
                throw new TokenYaUsadoException("El token ya fue usado.");
            }
            throw new IllegalStateException("Token inválido.");
        }

        // Ahora el token está marcado como usado en la BD; obtener registro para conocer el usuario
        TokenResetPassword tokenEntity = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalStateException("Token no encontrado tras marcarlo como usado"));

        // Validaciones de contraseña
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres.");
        }
        if (nuevaPassword.length() > 16) {
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.");
        }

        Usuario usuario = tokenEntity.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // tokenEntity.usado ya fue puesto a true por la actualización condicional; opcionalmente refrescar/save si lo deseas
    }
    */
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