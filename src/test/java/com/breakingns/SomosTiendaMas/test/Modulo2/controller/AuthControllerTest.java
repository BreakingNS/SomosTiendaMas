package com.breakingns.SomosTiendaMas.test.Modulo2.controller;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.shared.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.servlet.http.Cookie;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*                                                  AuthControllerIntegrationTest
    
    Registro de usuario

        1. Registro exitoso con datos válidos
        2. Registro falla con email inválido
        3. Registro falla con username inválido
        4. Registro falla con password inválida
        5. Registro falla si el usuario ya existe
        6. Registro falla si el email ya existe

    Login

        7. Login exitoso con credenciales válidas
        8. Login falla con credenciales inválidas
        9. Login falla con usuario no existente

    Generación y validación de tokens

        10. Se generan access y refresh tokens al login
        11. Access token permite acceder a ruta protegida
        12. Refresh token permite obtener nuevos tokens

    Refresh de tokens

        13. Refresh exitoso con refresh token válido
        14. Refresh falla con refresh token inválido
        15. Refresh falla con refresh token expirado
        16. Refresh falla con refresh token revocado

    Logout individual y total

        17. Logout exitoso (revoca sesión y tokens)
        18. Logout total cierra todas las sesiones del usuario
        19. Logout falla si el token es inválido

    Acceso a rutas protegidas

        20. Acceso permitido con token válido
        21. Acceso denegado sin token
        22. Acceso denegado con token inválido
        23. Acceso denegado con token expirado

    Acceso según rol

        24. Acceso permitido a rutas de admin solo con rol admin
        25. Acceso denegado a rutas de admin con rol usuario

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
class AuthControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    
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

    // Registro de usuario

    // 1. Registro exitoso con datos válidos
    @Test
    void registroUsuario_exitoso() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("nuevoUsuario", "nuevo@correo.com", "P123456");
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    // 2. Registro falla con email inválido
    @Test
    void registroUsuario_emailInvalido_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("usuarioEmailInvalido", "correo-invalido", "P123456");
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 3. Registro falla con username inválido
    @Test
    void registroUsuario_usernameInvalido_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("", "usuario@correo.com", "P123456");
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 4. Registro falla con password inválida
    @Test
    void registroUsuario_passwordInvalida_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("usuarioPassInvalida", "usuario@correo.com", "123");
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 5. Registro falla si el usuario ya existe
    @Test
    void registroUsuario_usuarioExistente_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("usuarioExistente", "usuarioExistente@correo.com", "P123456");
        // Primer registro exitoso
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        // Segundo registro con mismo username
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 6. Registro falla si el email ya existe
    @Test
    void registroUsuario_emailExistente_falla() throws Exception {
        RegistroUsuarioDTO registroDTO1 = new RegistroUsuarioDTO("usuarioEmail1", "emailrepetido@correo.com", "P123456");
        RegistroUsuarioDTO registroDTO2 = new RegistroUsuarioDTO("usuarioEmail2", "emailrepetido@correo.com", "P123456");
        // Primer registro exitoso
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO1)))
                .andReturn();
        // Segundo registro con mismo email
        MvcResult result = mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO2)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // Login

    // 7. Login exitoso con credenciales válidas
    @Test
    void login_exitoso() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("loginUser", "login@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("loginUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(200, loginResult.getResponse().getStatus());
    }

    // 8. Login falla con credenciales inválidas
    @Test
    void login_credencialesInvalidas_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("loginUserFail", "loginfail@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("loginUserFail", "contraFail");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(401, loginResult.getResponse().getStatus());
    }

    // 9. Login falla con usuario no existente
    @Test
    void login_usuarioNoExistente_falla() throws Exception {
        LoginRequest loginRequest = new LoginRequest("usuarioNoExiste", "cualquierPass");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(401, loginResult.getResponse().getStatus());
    }

    // Generación y validación de tokens

    // 10. Se generan access y refresh tokens al login
    @Test
    void login_generacionTokens_ok() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("tokenUser", "token@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("tokenUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

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
    }

    // 11. Access token permite acceder a ruta protegida
    @Test
    void accessToken_accesoRutaProtegida_ok() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("userProtegido", "protegido@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("userProtegido", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // 12. Refresh token permite obtener nuevos tokens
    @Test
    void refreshToken_obtieneNuevosTokens_ok() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("userRefresh", "refresh@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("userRefresh", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {

            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, refreshResult.getResponse().getStatus());
    }

    // Refresh de tokens

    // 13. Refresh exitoso con refresh token válido
    @Test
    void refreshToken_exitoso() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("refreshOkUser", "refreshok@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("refreshOkUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String refreshToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, refreshResult.getResponse().getStatus());
    }

    // 14. Refresh falla con refresh token inválido
    @Test
    void refreshToken_invalido_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("refreshFailUser", "refreshfail@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        String refreshTokenInvalido = "tokenInvalido123";
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/public/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshTokenInvalido + "\"}"))
                .andReturn();

        assertEquals(401, refreshResult.getResponse().getStatus());
    }

    // 15. Refresh falla con refresh token expirado
    @Test
    void refreshToken_expirado_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("refreshExpUser", "refreshexp@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("refreshExpUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        loginResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        cookies = loginResult.getResponse().getCookies();
        refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        Thread.sleep(5000);

        MvcResult result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // 16. Refresh falla con refresh token revocado
    @Test
    void refreshToken_revocado_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("refreshExpUser", "refreshexp@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("refreshExpUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

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

        refreshTokenService.revocarToken(refreshToken);

        MvcResult result = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // Logout individual y total

    // 17. Logout exitoso (revoca sesión y tokens)
    @Test
    void logout_exitoso() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("logoutUser", "logout@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("logoutUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, logoutResult.getResponse().getStatus());

        // Verificar que el access token está revocado en la BD
        var tokenEmitidoOpt = tokenEmitidoRepository.findByToken(accessToken);
        assertTrue(tokenEmitidoOpt.isPresent());
        assertTrue(tokenEmitidoOpt.get().isRevocado());

        // Verificar que el refresh token está revocado en la BD
        var sesionOpt = sesionActivaRepository.findByToken(accessToken);
        assertTrue(sesionOpt.isPresent());
        var refreshToken = sesionOpt.get().getRefreshToken();
        var refreshTokenOpt = refreshTokenRepository.findByToken(refreshToken);
        assertTrue(refreshTokenOpt.isPresent());
        assertTrue(refreshTokenOpt.get().getRevocado());
    }

    // 18. Logout total cierra todas las sesiones del usuario
    @Test
    void logoutTotal_revocaTodasSesionesYTokens() throws Exception {
        // Registrar usuario
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("multiSesionUser", "multi@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        
        // Login 1: user-agent y IP distintos
        LoginRequest loginRequest = new LoginRequest("multiSesionUser", "P123456");
        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent1")
                .with(request -> { request.setRemoteAddr("1.1.1.1"); return request; }))
                .andReturn();

        // Login 2
        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent2")
                .with(request -> { request.setRemoteAddr("2.2.2.2"); return request; }))
                .andReturn();

        // Login 3
        MvcResult loginResult3 = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent3")
                .with(request -> { request.setRemoteAddr("3.3.3.3"); return request; }))
                .andReturn();

        // Obtener access token de la última sesión (para hacer logout total)
        String accessToken = null;
        for (Cookie cookie : loginResult3.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);
        
        // Logout total
        MvcResult logoutTotalResult = mockMvc.perform(post("/api/auth/private/logout-total")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, logoutTotalResult.getResponse().getStatus());
        
        // Validar que todas las sesiones del usuario están revocadas
        var sesiones = sesionActivaRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(sesiones.stream().allMatch(SesionActiva::isRevocado), "Todas las sesiones deben estar revocadas");

        // Validar que todos los access tokens están revocados
        var accessTokens = tokenEmitidoRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(accessTokens.stream().allMatch(t -> t.isRevocado()), "Todos los access tokens deben estar revocados");

        // Validar que todos los refresh tokens están revocados
        var refreshTokens = refreshTokenRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(refreshTokens.stream().allMatch(t -> t.getRevocado()), "Todos los refresh tokens deben estar revocados");
    }

    // 19. Logout falla si el token es inválido
    @Test
    void logout_tokenInvalido_falla() throws Exception {
        String accessTokenInvalido = "tokenInvalido123";
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessTokenInvalido)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, logoutResult.getResponse().getStatus());
    }

    // Acceso a rutas protegidas

    // 20. Acceso permitido con token válido
    @Test
    void accesoRutaProtegida_tokenValido_ok() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("accesoUser", "acceso@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("accesoUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // 21. Acceso denegado sin token
    @Test
    void accesoRutaProtegida_sinToken_falla() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // 22. Acceso denegado con token inválido
    @Test
    void accesoRutaProtegida_tokenInvalido_falla() throws Exception {
        String accessTokenInvalido = "tokenInvalido123";
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessTokenInvalido)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // 23. Acceso denegado con token expirado
    @Test
    void accesoRutaProtegida_tokenExpirado_falla() throws Exception {
        RegistroUsuarioDTO registroDTO = new RegistroUsuarioDTO("refreshExpUser", "refreshexp@correo.com", "P123456");
        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        LoginRequest loginRequest = new LoginRequest("refreshExpUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        assertNotNull(refreshToken);

        loginResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }

        Thread.sleep(2000);

        MvcResult result = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // Acceso según rol
    
    // 24. Acceso permitido a rutas de admin solo con rol admin
    @Test
    void soloAdminPuedeVerSesionesDeUsuarios_controlado() throws Exception {
        // Registrar admin por endpoint
        Usuario admin = new Usuario();
        admin.setUsername("adminTest");
        admin.setEmail("admin@test.com");
        admin.setPassword("P123456");
        int statusAdmin = mockMvc.perform(post("/api/registro/public/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(admin)))
                .andReturn().getResponse().getStatus();
        assertEquals(200, statusAdmin);

        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("adminTest");
        assertTrue(adminOpt.isPresent());

        // Registrar usuario 1
        int statusUser1 = registrarUsuario("usuario1", "P123456", "user1@test.com");
        assertEquals(200, statusUser1);
        Optional<Usuario> user1Opt = usuarioRepository.findByUsername("usuario1");
        assertTrue(user1Opt.isPresent());
        Usuario usuario1 = user1Opt.get();

        // Registrar usuario 2
        int statusUser2 = registrarUsuario("usuario2", "P123456", "user2@test.com");
        assertEquals(200, statusUser2);
        Optional<Usuario> user2Opt = usuarioRepository.findByUsername("usuario2");
        assertTrue(user2Opt.isPresent());
        Usuario usuario2 = user2Opt.get();

        // Login admin
        MvcResult loginResultAdmin = loginUsuario("adminTest", "P123456");
        assertEquals(200, loginResultAdmin.getResponse().getStatus());
        String adminAccessToken = null;
        for (Cookie cookie : loginResultAdmin.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                adminAccessToken = cookie.getValue();
            }
        }
        assertNotNull(adminAccessToken);

        // Login usuario 1
        MvcResult loginResultUser1 = loginUsuario("usuario1", "P123456");
        assertEquals(200, loginResultUser1.getResponse().getStatus());
        String user1AccessToken = null;
        for (Cookie cookie : loginResultUser1.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                user1AccessToken = cookie.getValue();
            }
        }
        assertNotNull(user1AccessToken);

        // Login usuario 2
        MvcResult loginResultUser2 = loginUsuario("usuario2", "P123456");
        assertEquals(200, loginResultUser2.getResponse().getStatus());
        String user2AccessToken = null;
        for (Cookie cookie : loginResultUser2.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                user2AccessToken = cookie.getValue();
            }
        }
        assertNotNull(user2AccessToken);

        // Admin accede y puede ver sesiones activas de usuario 1
        MvcResult resultAdminUser1 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + adminAccessToken)
                .param("idUsuario", String.valueOf(usuario1.getIdUsuario()))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, resultAdminUser1.getResponse().getStatus());

        // Admin accede y puede ver sesiones activas de usuario 2
        MvcResult resultAdminUser2 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + adminAccessToken)
                .param("idUsuario", String.valueOf(usuario2.getIdUsuario()))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, resultAdminUser2.getResponse().getStatus());
    }

    // 25. Acceso denegado a rutas de admin con rol usuario
    @Test
    void accesoAdmin_conRolUsuario_falla() throws Exception {
        // Registrar admin por endpoint
        Usuario admin = new Usuario();
        admin.setUsername("adminTest");
        admin.setEmail("admin@test.com");
        admin.setPassword("P123456");
        int statusAdmin = mockMvc.perform(post("/api/registro/public/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(admin)))
                .andReturn().getResponse().getStatus();
        assertEquals(200, statusAdmin);

        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("adminTest");
        assertTrue(adminOpt.isPresent());

        // Registrar usuario 1
        int statusUser1 = registrarUsuario("usuario1", "P123456", "user1@test.com");
        assertEquals(200, statusUser1);
        Optional<Usuario> user1Opt = usuarioRepository.findByUsername("usuario1");
        assertTrue(user1Opt.isPresent());

        // Registrar usuario 2
        int statusUser2 = registrarUsuario("usuario2", "P123456", "user2@test.com");
        assertEquals(200, statusUser2);
        Optional<Usuario> user2Opt = usuarioRepository.findByUsername("usuario2");
        assertTrue(user2Opt.isPresent());

        // Login admin
        MvcResult loginResultAdmin = loginUsuario("adminTest", "P123456");
        assertEquals(200, loginResultAdmin.getResponse().getStatus());
        String adminAccessToken = null;
        for (Cookie cookie : loginResultAdmin.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                adminAccessToken = cookie.getValue();
            }
        }
        assertNotNull(adminAccessToken);

        // Login usuario 1
        MvcResult loginResultUser1 = loginUsuario("usuario1", "P123456");
        assertEquals(200, loginResultUser1.getResponse().getStatus());
        String user1AccessToken = null;
        for (Cookie cookie : loginResultUser1.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                user1AccessToken = cookie.getValue();
            }
        }
        assertNotNull(user1AccessToken);

        // Login usuario 2
        MvcResult loginResultUser2 = loginUsuario("usuario2", "P123456");
        assertEquals(200, loginResultUser2.getResponse().getStatus());
        String user2AccessToken = null;
        for (Cookie cookie : loginResultUser2.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                user2AccessToken = cookie.getValue();
            }
        }
        assertNotNull(user2AccessToken);

        // Usuario común intenta acceder a endpoint solo admin (debe fallar con 403)
        MvcResult resultUser1 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + user1AccessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(403, resultUser1.getResponse().getStatus());
    }
}