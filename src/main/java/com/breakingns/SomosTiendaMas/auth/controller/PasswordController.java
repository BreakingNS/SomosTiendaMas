package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.ChangePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.EmailRequest;
import com.breakingns.SomosTiendaMas.auth.dto.OlvidePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.service.PasswordResetService;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordController {
    
    private final AuthService authService;
    private final UsuarioServiceImpl usuarioService;
    private final IUsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    private final PasswordResetService passwordResetService;

    public PasswordController(AuthService authService, UsuarioServiceImpl usuarioService, IUsuarioRepository usuarioRepository, LoginAttemptService loginAttemptService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService;
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        String email = emailRequest.email();
        String ip = RequestUtil.obtenerIpCliente(request);

        authService.procesarSolicitudOlvidePassword(email, ip, request);

        return ResponseEntity.ok(Map.of(
            "message", "Si el email existe, te enviaremos instrucciones para recuperar tu contraseña."
        ));
    }
    
    /*
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        String email = emailRequest.email();
        String ip = RequestUtil.obtenerIpCliente(request);

        if (loginAttemptService.isBlocked(email, ip)) {
            throw new TooManyRequestsException("Demasiadas solicitudes, intenta más tarde.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            authService.solicitarRecuperacionPassword(email, request);
        } else {
            loginAttemptService.loginFailed(null, ip);
        }

        return ResponseEntity.ok(Map.of(
            "message", "Si el email existe, te enviaremos instrucciones para recuperar tu contraseña."
        ));
    }*/
    
    /*
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        String email = emailRequest.email();
        String ip = RequestUtil.obtenerIpCliente(request);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            authService.solicitarRecuperacionPassword(emailRequest.email(), request);
        } else {
            loginAttemptService.loginFailed(null, ip);
        }

        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> solicitarRecuperacionPassword(@Valid @RequestBody EmailRequest emailRequest, HttpServletRequest request) {
        authService.solicitarRecuperacionPassword(emailRequest.email(), request);
        return ResponseEntity.ok(Map.of("message", "Si el email existe, te enviaremos instrucciones para recuperar tu contraseña."));
    }*/
        
    /*
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> solicitarRecuperacionPassword(@Valid @RequestBody OlvidePasswordRequest request) {
        authService.solicitarRecuperacionPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "Si el email existe, te enviaremos instrucciones para recuperar tu contraseña."));
        */
        /*
            Sugerencia mínima (no urgente):
            Podrías extraer la lógica del token a un PasswordResetService o 
            TokenResetService si querés dejar el AuthService más limpio, pero 
            no es necesario ahora.
        */
    //}
    
    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetearPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
    
    @PostMapping("/private/change-password")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req, Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        passwordResetService.changePassword(usuario, req.currentPassword(), req.newPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }
    
    @PostMapping("/public/generartokenexpirado") // SOLO PRUEBAS
    public ResponseEntity<?> generarTokenExpirado(@Valid @RequestBody OlvidePasswordRequest request) {
        authService.generarTokenExpirado(request.email());
        return ResponseEntity.ok("Si el email existe, te enviaremos instrucciones para recuperar tu contraseña.");
    }
    
    @PostMapping("/public/generartokenusado") // SOLO PRUEBAS
    public ResponseEntity<?> generarTokenUsado(@Valid @RequestBody OlvidePasswordRequest request) {
        authService.generarTokenUsado(request.email());
        return ResponseEntity.ok("Si el email existe, te enviaremos instrucciones para recuperar tu contraseña.");
    }
    
}