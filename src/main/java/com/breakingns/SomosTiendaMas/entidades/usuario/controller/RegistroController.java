package com.breakingns.SomosTiendaMas.entidades.usuario.controller;
/* 
import com.breakingns.SomosTiendaMas.auth.dto.request.ChangePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.EmailRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.PasswordResetService;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/registro")
public class RegistroController {
    
    private final UsuarioServiceImpl usuarioService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public RegistroController(UsuarioServiceImpl usuarioService, 
                                AuthService authService, 
                                PasswordResetService passwordResetService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/public/usuario")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegistroUsuarioDTO registroDTO,
                                               HttpServletRequest request) {
        String ip = RequestUtil.obtenerIpCliente(request);
        usuarioService.registrarConRolDesdeDTO(registroDTO, ip);
        return ResponseEntity.ok("Usuario registrado correctamente.");
    }

    @PostMapping("/public/olvide-password")
    public ResponseEntity<?> olvidePassword(@RequestBody @Valid EmailRequest emailRequest, HttpServletRequest request) {
        String email = emailRequest.email();
        String ip = RequestUtil.obtenerIpCliente(request);

        authService.procesarSolicitudOlvidePassword(email, ip, request);

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
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest req, Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        passwordResetService.changePassword(usuario, req.currentPassword(), req.newPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }
}*/
