package com.breakingns.SomosTiendaMas.test.service;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.service.PasswordResetService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.auth.utils.UsuarioUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.security.exception.TokenException;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private TokenEmitidoService tokenEmitidoService;
    @Mock
    private SesionActivaService sesionActivaService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private UsuarioServiceImpl usuarioService;
    @Mock
    private ISesionActivaRepository sesionActivaRepository;
    @Mock
    private IRefreshTokenRepository refreshTokenRepository;
    @Mock
    private UsuarioUtils usuarioUtils;
    
    private final String accessToken = "accessTokenEjemplo";
    private final String refreshTokenStr = "refreshTokenEjemplo";
    /*
    @Test
    void logout_deberiaRevocarTodoCorrectamente() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("usuarioTest");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUsuario(usuario);
        refreshToken.setRevocado(false);

        when(jwtTokenProvider.obtenerUsernameDelToken(accessToken)).thenReturn("usuarioTest");
        when(usuarioRepository.findByUsername("usuarioTest")).thenReturn(Optional.of(usuario));
        when(refreshTokenRepository.findByToken(refreshTokenStr)).thenReturn(Optional.of(refreshToken));

        // Act
        authService.logout(accessToken, refreshTokenStr);

        // Assert
        verify(refreshTokenService).logout(refreshTokenStr);
        verify(tokenEmitidoService).revocarToken(accessToken);
        verify(sesionActivaService).revocarSesion(accessToken);
    }*/
    
    @Test
    void logout_deberiaLanzarExcepcionSiRefreshTokenNoPerteneceAlUsuario() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2L);
        otroUsuario.setUsername("otroUsuario");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("usuarioTest");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUsuario(otroUsuario); // NO es el usuario del accessToken

        when(jwtTokenProvider.obtenerUsernameDelToken(accessToken)).thenReturn("usuarioTest");
        when(usuarioRepository.findByUsername("usuarioTest")).thenReturn(Optional.of(usuario));
        when(refreshTokenRepository.findByToken(refreshTokenStr)).thenReturn(Optional.of(refreshToken));

        // Act + Assert
        TokenException exception = assertThrows(TokenException.class, () -> {
            authService.logout(accessToken);
        });
        assertEquals("El refresh token no pertenece al usuario autenticado", exception.getMessage());
        
        verifyNoInteractions(refreshTokenService, tokenEmitidoService, sesionActivaService);
    }
    
    @Test
    void logout_deberiaLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        when(jwtTokenProvider.obtenerUsernameDelToken(accessToken)).thenReturn("usuarioTest");
        when(usuarioRepository.findByUsername("usuarioTest")).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.logout(accessToken);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verifyNoInteractions(refreshTokenService, tokenEmitidoService, sesionActivaService);
    }
    
    // 8) Simular fallo de DB durante recuperación de contraseña:
    @Test
    void testErrorRecuperacionPasswordPorFalloDB() {
        String emailFallido = "falla@db.com";
        String ip = "127.0.0.1";

        // Simulamos que la DB lanza una excepción al buscar el email
        when(usuarioRepository.findByEmail(emailFallido))
            .thenThrow(new DataAccessException("Error de conexión") {});

        // Como LoginAttemptService está mockeado, decimos que no está bloqueado
        when(loginAttemptService.isBlocked(emailFallido, ip)).thenReturn(false);

        // No necesitamos mockear request para este test, puede ser null
        HttpServletRequest request = null;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.procesarSolicitudOlvidePassword(emailFallido, ip, request);
        });

        assertTrue(exception.getMessage().contains("Error de conexión"));

        // Verificamos que no se llamó al passwordResetService porque falló antes
        verifyNoInteractions(passwordResetService);
    }
    
}