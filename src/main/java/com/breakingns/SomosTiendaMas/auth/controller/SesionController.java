package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.DatosComprobacionSesionDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.TokenInvalidoException;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.utils.CookieUtils;
import com.breakingns.SomosTiendaMas.utils.TokenUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {
    
    private final AuthService authService;

    private final SesionActivaService sesionActivaService;
    private final TokenEmitidoService tokenEmitidoService;
    
    private final IUsuarioRepository usuarioRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public SesionController(AuthService authService, 
    SesionActivaService sesionActivaService, 
    TokenEmitidoService tokenEmitidoService, 
    IUsuarioRepository usuarioRepository,
    JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.sesionActivaService = sesionActivaService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /*                                   Endpoints:
        1. Listar mis sesiones activas (Admin/User)
        2. Listar Sesiones Activas (Admin)
        3. Revocar Sesión Activa (Admin)
        4. Logout Otras Sesiones (User/Admin)

        5. Datos de Sesion (Todos los usuarios): muestra datos en una esquina del frontend:
            - Sesion: Activa / Inactiva
            - Usuario: Nombre de usuario
            - Rol: Rol del usuario
            - JWT: Fecha de expiracion del token JWT
            - Refresh: Fecha de expiracion del token Refresh
    */

    // Private con acceso a usuarios autenticados
    @GetMapping("/private/activas")
    @PreAuthorize("isAuthenticated()")
    public List<SesionActivaDTO> misSesionesActivas() {
        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();
        return sesionActivaService.listarSesionesActivas(idUsuario);
    }

    @PostMapping("/private/admin/revocar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revocarSesionActiva(@RequestParam Long idSesion) {
        sesionActivaService.revocarSesionPorId(idSesion);
        return ResponseEntity.ok("Sesión revocada correctamente");
    }
    
    @PostMapping("/private/logout-otras-sesiones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutOtrasSesiones(
        @RequestHeader("Authorization") String authorizationHeader) {

        String accessToken = TokenUtils.extractTokenFromHeader(authorizationHeader);
        if (accessToken == null) {
            throw new TokenInvalidoException("Token faltante o mal formado");
        }

        if (!jwtTokenProvider.validarToken(accessToken)) {
            throw new TokenInvalidoException("Token inválido o expirado");
        }

        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();

        // Verificar que el ID extraído del token corresponda al accessToken actual
        if (!jwtTokenProvider.validarTokenPorId(accessToken, idUsuario)) {
            throw new TokenInvalidoException("El token no corresponde a la sesión actual.");
        }

        // Ahora solo pasás el accessToken, el service buscará el refresh asociado
        authService.logoutTotalExceptoSesionActual(idUsuario, accessToken);
        return ResponseEntity.ok("Sesiones cerradas excepto la actual");
    }
    
    // Jerarquia alta
    @GetMapping("/private/admin/activas")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPERADMIN', 'ROLE_SOPORTE')")
    public List<SesionActivaDTO> listarSesionesActivas(@RequestParam(required = false) Long idUsuario) {
        return sesionActivaService.listarSesionesActivasComoAdmin(idUsuario);
    }
    
    // Test
    @GetMapping("/private/datos-sesion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerDatosDeSesion(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletRequest request) {

        // Si no llega Authorization header, intentar obtener token desde cookie
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            String tokenDesdeCookie = CookieUtils.getAccessTokenFromCookies(request);
            if (tokenDesdeCookie == null || tokenDesdeCookie.isBlank()) {
                throw new TokenInvalidoException("Authorization header no presente y no se encontró token en cookies");
            }
            authorizationHeader = "Bearer " + tokenDesdeCookie;
        }

        DatosComprobacionSesionDTO datosSesion = sesionActivaService.obtenerDatosDeSesion(authorizationHeader);
        return ResponseEntity.ok(datosSesion);
    }

    // Test
    @PostMapping("/public/verificar-email")
    public ResponseEntity<?> verificarEmailTest(@RequestParam String username) {
        Usuario user = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (Boolean.TRUE.equals(user.getEmailVerificado())) {
            return ResponseEntity.ok("Email ya verificado");
        }
        user.setEmailVerificado(true);
        usuarioRepository.saveAndFlush(user);
        return ResponseEntity.ok("Email marcado como verificado para tests");
    }
}
