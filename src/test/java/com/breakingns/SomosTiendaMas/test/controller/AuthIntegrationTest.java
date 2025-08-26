package com.breakingns.SomosTiendaMas.test.controller;

import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.RefreshTokenRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/*                                                  AuthIntegrationTest

        1. Verifica que un usuario cuando se registra, si existe en la BD - registroUsuario_deberiaCrearUsuario()

        2. Registro con campos invalidos - registroConCamposInvalidos()

                            Autenticacion (Login y Tokens)

        1. Login con credenciales incorrectas - loginIncorrecto()

        2. Login con usuario sin roles asignados - loginSinRolesAsignados()

        3. Verifica que se generaron correctamente los tokens - loginGeneraTokensCorrectamente()

        4. Obtiene un nuevo refresh (y un nuevo jwt) - nuevoRefresh()

        5. Intentar generar refresh con tokens invalidos - refreshTokenInvalido()

                            Logout

        6. Logout simple, un solo logout - logoutSimple()

        7. Logout simple con token invalido - logoutSimpleConTokenInvalido()

        8. Logout total - logoutTotal()

        9. Logout total con token invalido - logoutTotalConTokenInvalido()

                            Acceso y Autorizacion
        10. Acceso a Carrito de Usuario por Admin (Autorizado) - accesoACarritoUaA()

        11. Acceso a Carrito de Admin por Usuario (No Autorizado) - accesoACarritoAaU()

        12. Acceso a Carrito de Admin por Admin (Autorizado) - accesoACarritoAaA()

        13. Acceso a Carrito de Usuario por Usuario (Autorizado) - accesoACarritoUaU()

        14. Accede a ruta protegida sin token - accesoARutaProtegidaSinToken()

        15. Accede a ruta inexistente - accesoARutaInexistente()

        16. Accede a ruta protegida con token invalido - accesoARutaProtegidaConTokenInvalido()

*/

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
public class AuthIntegrationTest {
    
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
    private final JwtTokenProvider jwtTokenProvider;
    private final SesionActivaService sesionActivaService;
    private final UsuarioServiceImpl usuarioService;
    
    private String refreshAdmin;
    private String refreshUsuario;
    private String tokenAdmin;
    private String tokenUsuario;
    private Long idAdmin;
    private Long idUsuario;
    
    @BeforeEach
    void setUp() throws Exception {
        // Siempre registrar y loguear, sin condicionales
        registrarUsuario("admin", "987654", "admin@test.com");
        registrarUsuario("usuario", "123456", "usuario@test.com");
        
        AuthResponse adminAuth = loginYGuardarDatos("admin", "987654");
        tokenAdmin = adminAuth.getAccessToken();
        refreshAdmin = adminAuth.getRefreshToken();
        idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);

        AuthResponse userAuth = loginYGuardarDatos("usuario", "123456");
        tokenUsuario = userAuth.getAccessToken();
        refreshUsuario = userAuth.getRefreshToken();
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

        mockMvc.perform(post("/api/registro/public/" + username)
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
            tokenAdmin = jwtResponse.getAccessToken();
            refreshAdmin = jwtResponse.getRefreshToken();
            idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        } else if (username.equals("usuario")) {
            tokenUsuario = jwtResponse.getAccessToken();
            refreshUsuario = jwtResponse.getRefreshToken();
            idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
        }

        return new AuthResponse(jwtResponse.getAccessToken(), jwtResponse.getRefreshToken());
    }

    // Verifica que un usuario cuando se registra, si existe en la BD
    @Test
    void registroUsuario_deberiaCrearUsuario() throws Exception {
        assertFalse(usuarioService.existeUsuario("usuarioPrueba"));
        
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioPrueba");
        usuario.setPassword("abcdef");
        usuario.setEmail("usuario2@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
        .andExpect(status().isOk())
        .andExpect(content().string("Usuario registrado correctamente."));
        
        assertTrue(usuarioService.existeUsuario("usuarioPrueba"));
    }
    
    // Registro con campos invalidos
    @Test
    void registroConCamposInvalidos() throws Exception {
        assertFalse(usuarioService.existeUsuario("usuarioPrueba1"));
        
        Usuario usuario1 = new Usuario();
        usuario1.setUsername("");
        usuario1.setPassword("abcdef");
        usuario1.setEmail("usuario1@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario1)))
        .andExpect(status().isBadRequest());
        
        Usuario usuario2 = new Usuario();
        usuario2.setUsername("abcdef");
        usuario2.setPassword("");
        usuario2.setEmail("usuario2@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario2)))
        .andExpect(status().isBadRequest());
        
        Usuario usuario3 = new Usuario();
        usuario3.setUsername("");
        usuario3.setPassword("");
        usuario3.setEmail("usuario3@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario3)))
        .andExpect(status().isBadRequest());
        
        //Usuario ya registrado.
        Usuario usuario4 = new Usuario();
        usuario4.setUsername("usuario");
        usuario4.setPassword("asdfhgi");
        usuario4.setEmail("usuario4@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario4)))
        .andExpect(status().isBadRequest());
        
        //Email ya registrado.
        Usuario usuario5 = new Usuario();
        usuario5.setUsername("usuario");
        usuario5.setPassword("asdfhgi");
        usuario5.setEmail("usuario4@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario5)))
        .andExpect(status().isBadRequest());
    }
    
    
    @Test
    void testPruebaTest(){assertEquals(true, true);}
        
    
    // 1) Login con credenciales incorrectas
    @Test
    void loginIncorrecto() throws Exception {
        assertEquals(refreshAdmin, refreshAdmin);
        
        // Login del usuario
        LoginRequest loginUsuario = new LoginRequest("usuario", "holamundo");

        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUsuario))
                .header("User-Agent", "MockMvc") // 🛠️ importante
                .with(request -> {
                    request.setRemoteAddr("127.0.0.2"); // 🛠️ importante
                    return request;
                }))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }
    
    // 2) Login con usuario sin roles asignados
    @Test
    void loginSinRolesAsignados() throws Exception {
        registrarUsuarioSinRoles("usuario_sin_roles", "456789", "usuarioSR@test.com");

        LoginRequest loginRequest = new LoginRequest("usuario_sin_roles", "456789");

        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "JUnit-Test"))
            .andExpect(status().isUnauthorized()); // 401 esperado si no tiene roles
    }
    
    // 3) Verifica que se generaron correctamente los tokens
    @Test
    void loginGeneraTokensCorrectamente() {
        assertNotNull(tokenAdmin);
        assertFalse(tokenAdmin.isBlank());
        assertNotNull(tokenUsuario);
        assertFalse(tokenUsuario.isBlank());

        System.out.println("Token admin: " + tokenAdmin);
        System.out.println("Token user: " + tokenUsuario);
    }
    
    // 4) Obtiene un nuevo refresh (y un nuevo jwt)
    @Test
    void nuevoRefresh() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshUsuario);

        MvcResult result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "MockMvc") // opcional, si lo usás en el backend
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1"); // opcional, si validás IP
                    return req;
                }))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotEquals(tokenUsuario, authResponse.getAccessToken()); // opcional
    }
    
    // 5) Intentar generar refresh con tokens invalidos
    @Test
    void refreshTokenInvalido() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refresh_invalido");

        mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + tokenUsuario) // token válido o inválido, depende de tu lógica
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest))
                .header("User-Agent", "MockMvc") // si lo usás
                .with(req -> {
                    req.setRemoteAddr("127.0.0.1");
                    return req;
                }))
            .andDo(print()) // ayuda a ver qué devuelve si sigue fallando
            .andExpect(status().isForbidden()) // o .isUnauthorized() según tu handler
            .andExpect(jsonPath("$.message").value("Refresh token no válido."));
    }
    
    // 6) Logout simple, un solo logout
    @Test
    void logoutSimple() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
        
        RefreshTokenRequest request = new RefreshTokenRequest(refreshUsuario);
        
        System.out.println("Por hacer el logout");
        mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Sesion cerrada correctamente"));
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isUnauthorized());
    }
    
    // 7) Logout simple con token invalido
    @Test
    void logoutSimpleConTokenInvalido() throws Exception {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("refreshValidoPeroAccessInvalido");

        mockMvc.perform(post("/api/auth/private/logout") // <-- Endpoint
                .header("Authorization", "Bearer token_invalido") // <-- Token inválido
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
            .andExpect(status().isUnauthorized()); // <-- 401 por token de acceso inválido
    }
    
    // 8) Logout total   
    @Test
    void logoutTotal() throws Exception {
        // Primero logueamos y conseguimos un token válido
        loginYGuardarDatos("usuario", "123456");
        loginYGuardarDatos("usuario", "123456");
        
        LoginRequest loginRequest = new LoginRequest("usuario", "123456");

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

        assertEquals(4, sesionActivaService.numeroSesionesActivas(idUsuario));
        
        AuthResponse jwtResponse = objectMapper.readValue(response, AuthResponse.class);

        mockMvc.perform(post("/api/auth/private/logout-total") // <-- Endpoint
                .header("Authorization", "Bearer " + jwtResponse.getAccessToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Sesiones cerradas en todos los dispositivos"));
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isUnauthorized());
        
        assertEquals(0, sesionActivaService.numeroSesionesActivas(idUsuario));
    }
    
    // 9) Logout total con token invalido    
    @Test
    void logoutTotalConTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/private/logout-total") // <-- Endpoint
                .header("Authorization", "Bearer token_invalido")) // <-- Token inválido
            .andExpect(status().isUnauthorized()); // <-- Esperamos 401
    }
    
    // 10) Acceso a Carrito de Usuario por Admin (Autorizado)
    @Test
    void accesoACarritoUaA() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
    
    // 11) Acceso a Carrito de Admin por Usuario (No Autorizado)
    @Test
    void accesoACarritoAaU() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }
    
    // 12) Acceso a Carrito de Admin por Admin (Autorizado)
    @Test
    void accesoACarritoAaA() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
    
    // 13) Acceso a Carrito de Usuario por Usuario (Autorizado)
    @Test
    void accesoACarritoUaU() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
    }
    
    // 14) Accede a ruta protegida sin token
    @Test
    void accesoARutaProtegidaSinToken() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario))
                .andExpect(status().isUnauthorized());
    }
    
    // 15) Accede a ruta inexistente
    @Test
    void accesoARutaInexistente() throws Exception {
        mockMvc.perform(get("/api/algunaruta/traera/")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isNotFound());
    }    
    
    // 16) Accede a ruta protegida con token invalido
    @Test
    void accesoARutaProtegidaConTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario + "a"))
                .andExpect(status().isUnauthorized());
    }
    
}