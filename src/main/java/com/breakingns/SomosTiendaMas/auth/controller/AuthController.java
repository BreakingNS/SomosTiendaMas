package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.ChangePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.OlvidePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.dto.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.service.ResetPasswordService;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.auth.utils.TokenUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioService;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final CarritoService carritoService;
    private final RolService rolService;
    
    private final RefreshTokenService refreshTokenService;
    private final SesionActivaService sesionActivaService;
    private final TokenEmitidoService tokenEmitidoService;
    private final ResetPasswordService resetPasswordService;
    
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, UsuarioService usuarioService, CarritoService carritoService, RolService rolService, RefreshTokenService refreshTokenService, SesionActivaService sesionActivaService, TokenEmitidoService tokenEmitidoService, ResetPasswordService resetPasswordService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.carritoService = carritoService;
        this.rolService = rolService;
        this.refreshTokenService = refreshTokenService;
        this.sesionActivaService = sesionActivaService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.resetPasswordService = resetPasswordService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    // ---
    
    @PostMapping("/public/registro/usuario")
    public ResponseEntity<String> registerUser(@RequestBody Usuario usuario) {
        usuarioService.registrarConRol(usuario, RolNombre.ROLE_USUARIO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/public/registro/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody Usuario usuario) {
        usuarioService.registrarConRol(usuario, RolNombre.ROLE_ADMIN);
        return ResponseEntity.ok("Administrador registrado correctamente");
    }
    
    @PostMapping("/public/login") // LISTO
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse tokens = authService.login(loginRequest, request);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/public/refresh-token")
    public ResponseEntity<AuthResponse> refrescarToken(@RequestBody RefreshTokenRequest refresh, HttpServletRequest request) {
        AuthResponse response = refreshTokenService.refrescarTokens(refresh.getRefreshToken(), request);
        return ResponseEntity.ok(response);
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
        resetPasswordService.resetearPassword(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
    
    @PostMapping("/private/change-password")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        usuarioService.changePassword(usuario, req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }
    
    @PostMapping("/private/logout")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(accessToken, request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
    
    @PostMapping("/private/logout-total")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutTotal(@RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.replace("Bearer ", "");
        authService.logoutTotal(accessToken);
        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }
    
    @GetMapping("/private/activas")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public List<SesionActivaDTO> misSesionesActivas() {
        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();
        return sesionActivaService.obtenerSesionesActivasPorUsuario(idUsuario);
    }

    @GetMapping("/private/admin/activas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<SesionActivaDTO> listarSesionesActivas(@RequestParam(required = false) Long idUsuario) {
        return sesionActivaService.obtenerSesionesActivas(idUsuario);
    }
    
    /*
    @DeleteMapping("/private/logout/{idSesion}")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> cerrarSesion(@PathVariable Long idSesion) {
        sesionActivaService.cerrarSesion(idSesion);
        return ResponseEntity.ok("Sesión cerrada con éxito");
    }
    */
    
    @PostMapping("/private/logout-otras-sesiones") // Listo
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutOtrasSesiones(@RequestBody RefreshTokenRequest request,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        
        // Log para verificar el encabezado
        System.out.println("Header Authorization recibido: " + authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Token faltante o mal formado"));
        }

        // Extrae el token de acceso
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Verifica si el token es válido antes de hacer logout
        if (!TokenUtils.validarToken(accessToken, jwtTokenProvider)) {
            return TokenUtils.respuestaTokenInvalido();
        }

        String refresh = request.getRefreshToken();
        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();
        
        authService.logoutTotalExceptoSesionActual(idUsuario, accessToken, refresh);
        return ResponseEntity.ok("Sesiones cerradas excepto la actual");
    }
    
    /*
    PARA UTILIZAR COOKIESSSSSSSSSSSSSSSSS -------------------------------
    */

    /*
    PARA COOKIESSSSSSSSSSSSSSSSSSS ---------------------------
    */
    
}

