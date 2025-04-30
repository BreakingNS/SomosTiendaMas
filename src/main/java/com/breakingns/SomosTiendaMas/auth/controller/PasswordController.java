package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.ChangePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.OlvidePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.ResetPasswordService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioService;
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
    private final UsuarioService usuarioService;

    private final ResetPasswordService resetPasswordService;

    public PasswordController(AuthService authService, UsuarioService usuarioService, ResetPasswordService resetPasswordService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.resetPasswordService = resetPasswordService;
    }
    
    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> solicitarRecuperacionPassword(@RequestBody OlvidePasswordRequest request) {
        authService.solicitarRecuperacionPassword(request.email());
        return ResponseEntity.ok("Si el email existe, te enviaremos instrucciones para recuperar tu contraseña.");
        /*
            Sugerencia mínima (no urgente):
            Podrías extraer la lógica del token a un PasswordResetService o 
            TokenResetService si querés dejar el AuthService más limpio, pero 
            no es necesario ahora.
        */
    }
    
    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        resetPasswordService.resetearPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
    
    @PostMapping("/private/change-password")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        usuarioService.changePassword(usuario, req.currentPassword(), req.newPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }
    
}
