package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.request.ChangePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.EmailRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.service.PasswordResetService;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
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

    private final PasswordResetService passwordResetService;

    public PasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    /*                                   Endpoints:
        1. Olvide Password
        2. Reset Password
        3. Change Password
        
    */

    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody @Valid EmailRequest emailRequest, HttpServletRequest request) {
        String email = emailRequest.email();
        passwordResetService.solicitarRecuperacionPassword(email);
        return ResponseEntity.ok(Map.of(
            "message", "Si el email existe, te enviaremos instrucciones para recuperar tu contraseña."
        ));
    }
    
    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetearPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
    
    @PostMapping("/private/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest req, Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        passwordResetService.changePassword(usuario, req.currentPassword(), req.newPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }
    
}