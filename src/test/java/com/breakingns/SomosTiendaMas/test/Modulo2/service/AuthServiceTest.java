package com.breakingns.SomosTiendaMas.test.Modulo2.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.breakingns.SomosTiendaMas.auth.dto.shared.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import com.breakingns.SomosTiendaMas.security.exception.TokenRevocadoException;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

/*                                                  AuthServiceTest
    
    Lógica de autenticación

        1. loginConCredencialesCorrectas_exitoso
        2. loginConCredencialesIncorrectas_falla
    
    Generación de tokens
    
        3. generarTokensAlLogin_exitoso
        4. formatoYExpiracionTokens_generadosCorrectamente

    Validación y revocación de tokens
    
        5. validarAccessToken_valido
        6. validarRefreshToken_valido
        7. revocarAccessToken_noSePuedeUsar
        8. revocarRefreshToken_noSePuedeUsar
    
    Manejo de sesiones activas
    
        9. crearSesionActivaAlLogin
        10. revocarSesionActiva_logout
        11. listarSesionesActivasUsuario
        12. cerrarOtrasSesionesActivas_endpoint

    Validación de errores
    
        13. tokenExpiradoDebeRetornarUnauthorized
        14. usuarioNoEncontrado_lanzaExcepcion
        15. tokenRevocado_lanzaExcepcion_integracion
        16. refreshTokenInvalido_lanzaExcepcion_integracion

*/

@ActiveProfiles("test")
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
    )/*,
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
    )*/
})
class AuthServiceTest {

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

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    private final AuthService authService;
    
    @Mock
    private TokenEmitidoService tokenEmitidoService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private final IUsuarioRepository usuarioRepository;

    // Método para registrar un usuario
    private int registrarUsuario(String username, String password, String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setEmail(email);

        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andReturn();

        return result.getResponse().getStatus();
    }

    // Método para hacer login de un usuario
    private MvcResult loginUsuario(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc"))
                .andReturn();

        return loginResult;
    }

    // 1. Lógica de autenticación

    @Test
    void loginConCredencialesCorrectas_exitoso() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuPrueba", "P123456", "usuPrueba@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuPrueba", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener tokens de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
    }

    @Test
    void loginConCredencialesIncorrectas_falla() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuPrueba", "P123456", "usuPrueba@prueba.com");
        assertEquals(200, statusRegistro);

        // Intentar login con contraseña incorrecta
        MvcResult loginResult = loginUsuario("usuPrueba", "falloContrasenia");
        assertEquals(401, loginResult.getResponse().getStatus());
    }
    
    /*
    @Test
    void loginUsuarioDesactivado_falla() {
        // Crear usuario desactivado
        Usuario usuario = new Usuario(null, "usuario", "password", "prueba@test.com", null);
        usuario.setActivo(false);

        // Mockear el repositorio para devolver el usuario desactivado
        when(usuarioRepository.findByUsername("usuario")).thenReturn(Optional.of(usuario));

        // Crear LoginRequest y MockHttpServletRequest
        LoginRequest loginRequest = new LoginRequest("usuario", "password");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");

        // Verificar que lanza la excepción correspondiente
        assertThrows(UsuarioDesactivadoException.class, () -> {
            authService.login(loginRequest, request);
        });
    }*/
    
    // 2. Generación de tokens

    @Test
    void generarTokensAlLogin_exitoso() throws Exception {
        // Registrar usuario por endpoint (flujo real)
        int statusRegistro = registrarUsuario("usuPrueba", "P123456", "usuPrueba@prueba.com");
        assertEquals(200, statusRegistro); // O 201 según tu API

        // Verificar que el usuario existe en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuPrueba");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuPrueba", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener tokens de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        // Verificar que los tokens no sean nulos
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Verificar que el access token existe en la BD
        Optional<TokenEmitido> tokenEmitidoOpt = tokenEmitidoRepository.findByToken(accessToken);
        assertTrue(tokenEmitidoOpt.isPresent());
        assertFalse(tokenEmitidoOpt.get().isRevocado());

        // Verificar que el refresh token existe en la BD
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshToken);
        assertTrue(refreshTokenOpt.isPresent());
        assertFalse(refreshTokenOpt.get().getRevocado());

        // Verificar que la sesión activa existe en la BD
        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertFalse(sesiones.isEmpty());
        assertEquals(accessToken, sesiones.get(0).getToken());
        assertEquals(refreshToken, sesiones.get(0).getRefreshToken());
    }
    
    @Test
    void formatoYExpiracionTokens_generadosCorrectamente() throws Exception {
        // Registrar usuario por endpoint (flujo real)
        int statusRegistro = registrarUsuario("usuPrueba2", "P654321", "usuPrueba2@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuPrueba2", "P654321");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        final String accessToken = Arrays.stream(cookies)
            .filter(cookie -> "accessToken".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
        assertNotNull(accessToken);

        // Verificar formato del token (ejemplo: empieza con "ey" si es JWT)
        assertTrue(accessToken.startsWith("ey"), "El access token debería tener formato JWT");

        // Verificar expiración en la BD
        Optional<TokenEmitido> tokenEmitidoOpt = tokenEmitidoRepository.findByToken(accessToken);
        assertTrue(tokenEmitidoOpt.isPresent());
        Instant expiracion = tokenEmitidoOpt.get().getFechaExpiracion();
        assertNotNull(expiracion);
        assertTrue(expiracion.isAfter(Instant.now()), "La expiración debe ser en el futuro");
    }
    
    // 3. Validación y revocación de tokens
    
    @Test
    void validarAccessToken_valido() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuValido", "P123456", "usuValido@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuValido", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Validar el access token usando el servicio real
        boolean esValido = authService.validarAccessToken(accessToken);
        assertTrue(esValido, "El access token debería ser válido");
    }
    
    @Test
    void validarRefreshToken_valido() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuRefresh", "P654321", "usuRefresh@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuRefresh", "P654321");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Validar el refresh token usando el servicio real
        boolean esValido = authService.validarRefreshToken(refreshToken);
        assertTrue(esValido, "El refresh token debería ser válido");
    }
    
    @Test
    void revocarAccessToken_noSePuedeUsar() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuRevoca", "P123456", "usuRevoca@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuRevoca", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken);

        // Revocar el access token usando el servicio real
        authService.revocarAccessToken(accessToken);

        // Verificar que el token está revocado en la BD
        Optional<TokenEmitido> tokenEmitidoOpt = tokenEmitidoRepository.findByToken(accessToken);
        assertTrue(tokenEmitidoOpt.isPresent());
        assertTrue(tokenEmitidoOpt.get().isRevocado());

        // Verificar que no se puede usar el token (solo assertThrows)
        assertThrows(TokenRevocadoException.class, () -> authService.validarAccessToken(accessToken));
    }
    
    @Test
    void revocarRefreshToken_noSePuedeUsar() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuRevocaRefresh", "P654321", "usuRevocaRefresh@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuRevocaRefresh", "P654321");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Revocar el refresh token usando el servicio real
        authService.revocarRefreshToken(refreshToken);

        // Verificar que el token está revocado en la BD
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshToken);
        assertTrue(refreshTokenOpt.isPresent());
        assertTrue(refreshTokenOpt.get().getRevocado());

        // Verificar que no se puede usar el token
        boolean esValido = authService.validarRefreshToken(refreshToken);
        assertFalse(esValido, "El refresh token revocado no debería ser válido");
    }
    
    // 4. Manejo de sesiones activas

    @Test
    void crearSesionActivaAlLogin() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuSesion", "P123456", "usuSesion@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuSesion", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Verificar que el usuario existe en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuSesion");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Verificar que la sesión activa existe en la BD y no está revocada
        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertFalse(sesiones.isEmpty(), "Debe existir al menos una sesión activa para el usuario");
        SesionActiva sesion = sesiones.get(0);
        assertEquals(usuario.getIdUsuario(), sesion.getUsuario().getIdUsuario());
        assertFalse(sesion.isRevocado(), "La sesión activa no debe estar revocada");
    }
    
    @Test
    void revocarSesionActiva_logout() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuLogout", "P123456", "usuLogout@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuLogout", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access y refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Ejecutar logout real
        authService.logout(accessToken);

        // Verificar que la sesión activa está revocada en la BD
        Optional<SesionActiva> sesionOpt = sesionActivaRepository.findByToken(accessToken);
        assertTrue(sesionOpt.isPresent());
        assertTrue(sesionOpt.get().isRevocado(), "La sesión activa debe estar revocada después del logout");
    }
    
    @Test
    void listarSesionesActivasUsuario() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuListar", "P123456", "usuListar@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuListar", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Llamar al endpoint de sesiones activas (ajusta la URL según tu API)
        MvcResult sesionesResult = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificar que la respuesta contiene al menos una sesión activa
        String json = sesionesResult.getResponse().getContentAsString();
        // Si usás Jackson:
        SesionActivaDTO[] sesiones = objectMapper.readValue(json, SesionActivaDTO[].class);
        assertTrue(sesiones.length > 0, "Debe existir al menos una sesión activa para el usuario");
        assertTrue(Arrays.stream(sesiones).allMatch(s -> !s.revocado()), "Las sesiones no deben estar revocadas");
    }
    
    @Test
    void cerrarOtrasSesionesActivas_endpoint() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuMultiSesion", "P123456", "usuMultiSesion@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint (primera sesión)
        MvcResult loginResult1 = loginUsuario("usuMultiSesion", "P123456");
        assertEquals(200, loginResult1.getResponse().getStatus());
        String accessToken1 = null;
        for (Cookie cookie : loginResult1.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken1 = cookie.getValue();
            }
        }
        assertNotNull(accessToken1);

        // Login por endpoint (segunda sesión)
        MvcResult loginResult2 = loginUsuario("usuMultiSesion", "P123456");
        assertEquals(200, loginResult2.getResponse().getStatus());
        String accessToken2 = null;
        for (Cookie cookie : loginResult2.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken2 = cookie.getValue();
            }
        }
        assertNotNull(accessToken2);

        // Llamar al endpoint para cerrar otras sesiones (usando la segunda sesión como actual)
        mockMvc.perform(post("/api/sesiones/private/logout-otras-sesiones")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificar que solo queda la sesión actual activa
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuMultiSesion");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertEquals(1, sesiones.size(), "Solo debe quedar una sesión activa");
        assertEquals(accessToken2, sesiones.get(0).getToken(), "La sesión activa debe ser la actual");
    }
    
    // 5. Validación de errores

    @Test
    void tokenExpiradoDebeRetornarUnauthorized() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuPrueba", "P123456", "usuPrueba@prueba.com");
        assertEquals(200, statusRegistro);

        // Login y obtención de tokens
        MvcResult loginResult = loginUsuario("usuPrueba", "P123456");
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Refrescar el token para obtener uno nuevo y asegurar que el tiempo de expiración sea el esperado
        loginResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        cookies = loginResult.getResponse().getCookies();
        accessToken = null;
        refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // Esperar a que el access token expire (configurado en 3 segundos)
        Thread.sleep(3100);

        // Intentar acceder a un endpoint protegido con el token expirado
        MvcResult result = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus(), "El access token expirado debe retornar 401 Unauthorized");

        // Esperar a que el refresh token también expire (configurado en 6 segundos)
        Thread.sleep(3000);

        // Intentar refrescar el token con el refresh expirado
        result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus(), "El refresh token expirado debe retornar 401 Unauthorized");
    }
    
    @Test
    void usuarioNoEncontrado_lanzaExcepcion() throws Exception {
        // No registrar el usuario, solo intentar login
        LoginRequest loginRequest = new LoginRequest("usuarioNoExist", "ContraIncorre");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc"))
                .andReturn();

        // Verificar que la respuesta sea 401 Unauthorized
        assertEquals(401, loginResult.getResponse().getStatus());
    }
    
    @Test
    void tokenRevocado_lanzaExcepcion_integracion() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuRevocado", "P123456", "usuRevocado@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuRevocado", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken);

        // Revocar el token usando el service o endpoint de logout
        authService.revocarAccessToken(accessToken);

        // Ahora sí, al validar el token debe lanzar la excepción
        assertThrows(TokenRevocadoException.class, () -> authService.validarAccessToken(accessToken));
    }
    
    @Test
    void refreshTokenInvalido_lanzaExcepcion_integracion() throws Exception {
        // Registrar usuario por endpoint
        int statusRegistro = registrarUsuario("usuRefresh", "P123456", "usuRefreshInvalido@prueba.com");
        assertEquals(200, statusRegistro);

        // Login por endpoint
        MvcResult loginResult = loginUsuario("usuRefresh", "P123456");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(refreshToken);

        // Modificar el refresh token para que sea inválido
        String refreshTokenInvalido = refreshToken + "X";

        // Ahora sí, al validar el refresh token debe lanzar la excepción
        assertThrows(RefreshTokenException.class, () -> authService.validarRefreshToken(refreshTokenInvalido));
    }
}
