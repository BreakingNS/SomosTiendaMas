package com.breakingns.SomosTiendaMas.test.service;
/*
import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenResetPasswordRepository;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@RequiredArgsConstructor
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SqlGroup({
    @Sql(
        statements = {
            "DELETE FROM login_failed_attempts",
            "DELETE FROM tokens_reset_password",
            "DELETE FROM sesiones_activas",
            "DELETE FROM token_emitido",
            "DELETE FROM refresh_token",
            "DELETE FROM carrito",
            "DELETE FROM usuario_roles",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    ),
    @Sql(
        statements = {
            "DELETE FROM login_failed_attempts",
            "DELETE FROM tokens_reset_password",
            "DELETE FROM sesiones_activas",
            "DELETE FROM token_emitido",
            "DELETE FROM refresh_token",
            "DELETE FROM carrito",
            "DELETE FROM usuario_roles",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
})
public class RefreshTokenServiceTest {
    /*          Metodos:
        
        status().isOk() → 200
        status().isUnauthorized() → 401
        status().isForbidden() → 403
        status().isInternalServerError() → 500
        status().isBadRequest() → 400
        status().isCreated() → 201
        status().isNotFound() → 404
        status().isNoContent() → 204
    
    */
    /*
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    private final AuthController authController;
    
    private final SesionActivaService sesionActivaService;
    private final UsuarioServiceImpl usuarioService;
    private final RolService rolService;
    private final RefreshTokenService refreshTokenService;
    
    private final IUsuarioRepository usuarioRepository;
    private final ITokenResetPasswordRepository tokenResetPasswordRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    
    
    private String refreshAdmin;
    private String refreshUsuario;
    private String tokenAdmin;
    private String tokenUsuario;
    private Long idAdmin;
    private Long idUsuario;
    
    @BeforeEach
    void setUp() throws Exception {
        // Siempre registrar y loguear, sin condicionales
        registrarAdmin("admin", "987654", "admin@test.com");
        registrarUsuario("usuario", "123456", "usuario@test.com");
        
        AuthResponse adminAuth = loginYGuardarDatos("admin", "987654");
        tokenAdmin = adminAuth.accessToken();
        refreshAdmin = adminAuth.refreshToken();
        idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        
        AuthResponse userAuth = loginYGuardarDatos("usuario", "123456");
        tokenUsuario = userAuth.accessToken();
        refreshUsuario = userAuth.refreshToken();
        idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
    }

    // Método para registrar un usuario sin roles
    private void registrarUsuarioSinRoles(String username, String password, String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setEmail(email);

        mockMvc.perform(post("/api/registro/public/sinrol")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isOk());
    }
    
    // Método para registrar un usuario
    private void registrarUsuario(String username, String password, String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setEmail(email);

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isOk());
    }
    
    // Método para registrar un admin
    private void registrarAdmin(String username, String password, String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setEmail(email);

        mockMvc.perform(post("/api/registro/public/admin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isOk());
    }

    // Método para hacer login y guardar datos
    private AuthResponse loginYGuardarDatos(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        String response = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AuthResponse jwtResponse = objectMapper.readValue(response, AuthResponse.class);

        // Guardar el token y el refresh
        if (username.equals("admin")) {
            tokenAdmin = jwtResponse.accessToken();
            refreshAdmin = jwtResponse.refreshToken();
            idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        } else if (username.equals("usuario")) {
            tokenUsuario = jwtResponse.accessToken();
            refreshUsuario = jwtResponse.refreshToken();
            idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
        }
        
        return new AuthResponse(jwtResponse.accessToken(), jwtResponse.refreshToken());
    }
    
    private AuthResponse loginYGuardarDatosOtroIp(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        String response = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc")
                .with(request -> {
                    request.setRemoteAddr("127.0.0.9");
                    return request;
                }))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AuthResponse jwtResponse = objectMapper.readValue(response, AuthResponse.class);

        // Guardar el token y el refresh
        if (username.equals("admin")) {
            tokenAdmin = jwtResponse.accessToken();
            refreshAdmin = jwtResponse.refreshToken();
            idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        } else if (username.equals("usuario")) {
            tokenUsuario = jwtResponse.accessToken();
            refreshUsuario = jwtResponse.refreshToken();
            idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
        }
        
        return new AuthResponse(jwtResponse.accessToken(), jwtResponse.refreshToken());
    }
    /*
    // 1) crear RefreshToken, deberia Crear Un RefreshTokenValido
    @Test
    void crearRefreshToken_deberiaCrearUnRefreshTokenValido() {
        // Arrange
        Long userId = 1L;
        String ip = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        Usuario usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(userId);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuarioMock));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken token = refreshTokenService.crearRefreshToken(userId, ip, userAgent);

        // Assert
        assertNotNull(token);
        assertEquals(usuarioMock, token.getUsuario());
        assertNotNull(token.getToken());
        assertTrue(token.getExpiraEn().isAfter(LocalDateTime.now()));
        assertEquals(ip, token.getIpCreacion());
        assertEquals(userAgent, token.getUserAgent());
        assertFalse(token.isRevocado());
        assertFalse(token.isUsado());
    }
    */
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    //
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


/*

Lista de tests recomendados para RefreshTokenService
1. Creación de Refresh Token
crearRefreshToken_deberiaCrearUnRefreshTokenValido()
Verifica que se cree un token con todos los campos correctos (usuario, token único, fecha de expiración en el futuro, ip, userAgent, revocado=false, usado=false).

crearRefreshToken_usuarioNoEncontrado_deberiaLanzarExcepcion()
Probar que si el usuario no existe, lanza UsuarioNoEncontradoException.

2. Logout individual
logout_deberiaRevocarElTokenYSetearFechaRevocado()
Verifica que al llamar logout con un token válido, se seteen revocado=true, fechaRevocado con tiempo actual y usado=false.

logout_tokenNoEncontrado_deberiaLanzarExcepcion()
Si el token no existe, lanza RefreshTokenException.

logout_tokenYaRevocadoOUsado_deberiaLanzarExcepcion()
Si el token está revocado o usado, lanza excepción.

3. Logout total (revocar todos los tokens de un usuario)
logoutTotal_deberiaRevocarTodosLosTokensDelUsuario()
Revocar todos los tokens no revocados de un usuario por username.

logoutTotal_usuarioSinTokens_noDeberiaFallar()
No debe fallar si el usuario no tiene tokens activos.

4. Logout total excepto sesión actual
logoutTotalExceptoSesionActual_deberiaRevocarTodosMenosElActual()
Revoca todos los tokens activos del usuario excepto el token pasado.

logoutTotalExceptoSesionActual_usuarioSinTokens_noDeberiaFallar()
Sin tokens activos no debe fallar.

5. Búsqueda y validación de tokens
encontrarPorToken_tokenExistente_deberiaRetornarToken()
Buscar un token existente retorna Optional con token.

encontrarPorToken_tokenNoExistente_deberiaRetornarEmpty()
Buscar token no existente retorna Optional vacío.

verificarValidez_tokenValido_deberiaRetornarToken()
Token no revocado, no usado y no expirado debe retornar token.

verificarValidez_tokenRevocado_deberiaRetornarEmpty()
Token revocado debe retornar Optional vacío.

verificarValidez_tokenUsado_deberiaRetornarEmpty()
Token usado debe retornar Optional vacío.

verificarValidez_tokenExpirado_deberiaRetornarEmpty()
Token expirado debe retornar Optional vacío.

6. Verificación de expiración (método verificarExpiracion)
verificarExpiracion_tokenNoExpiradoYNoRevocado_deberiaRetornarToken()
Retorna token sin excepción.

verificarExpiracion_tokenExpirado_deberiaLanzarExcepcion()
Lanza excepción si token expiró.

verificarExpiracion_tokenRevocado_deberiaLanzarExcepcion()
Lanza excepción si token revocado.

7. Borrar tokens de usuario
borrarTokensDeUsuario_usuarioExistente_deberiaEliminarTokens()
Elimina todos los tokens del usuario.

borrarTokensDeUsuario_usuarioNoExistente_deberiaLanzarExcepcion()
Usuario no encontrado lanza excepción.

8. Refrescar tokens (flujo completo)
refrescarTokens_tokenRefreshValido_deberiaGenerarNuevosTokens()
Dado un refresh token válido, genera nuevo refresh y access token, registra sesión, revoca tokens anteriores.

refrescarTokens_tokenRefreshNoEncontrado_deberiaLanzarExcepcion()
Si no encuentra el refresh token, lanza excepción.

refrescarTokens_tokenRefreshRevocadoOUsado_deberiaLanzarExcepcion()
Lanza excepción si refresh token está revocado o usado.

refrescarTokens_tokenRefreshExpirado_deberiaLanzarExcepcion()
Lanza excepción si el refresh token expiró.

refrescarTokens_noHaySesionAsociada_deberiaLanzarExcepcion()
Si no se encuentra sesión activa para el access token, lanza excepción.

refrescarTokens_headerAuthorizationMalFormado_deberiaLanzarExcepcion()
Si el header Authorization no está presente o mal formado, lanza excepción.

9. Métodos privados (probados indirectamente a través de los públicos)
revocarTokens(List<RefreshToken>): verificar que setea revocado=true y fechaRevocado.

revocarYUsar(RefreshToken): setea revocado=true, usado=true y fechaRevocado.

revocarTokensAnteriores(String accessToken, RefreshToken refreshToken): verifica que revoca refreshToken y llama a revocarToken y revocarSesion.

generarYRegistrarTokens(Usuario, HttpServletRequest): genera nuevos tokens y registra sesión.

extraerAccessTokenDesdeHeader(HttpServletRequest): extrae el token correctamente o lanza excepción.

Extras útiles
Tests de concurrencia para crear o revocar tokens simultáneamente.

Tests de integración con el repositorio real y base H2 (si usás).

Validar que el token generado es único (puede ser test unitario o en integración).

Mockear HttpServletRequest para controlar IP y User-Agent en tests.

Si querés, te puedo ayudar también a armar ejemplos concretos para algunos de estos tests, usando JUnit + Mockito o el framework que estés usando.

*/