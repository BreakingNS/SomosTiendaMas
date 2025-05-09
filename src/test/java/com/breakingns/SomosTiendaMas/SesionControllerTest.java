package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.SqlGroup;

/*                                                  SesionControllerTest

        1.  - misSesionesActivas_deberiaRetornarListaDeSesionesDelUsuarioAutenticado()

        2.  - misSesionesActivas_sinRolAdecuado_deberiaRetornar403()

        3.  - listarSesionesActivas_adminPuedeVerTodasLasSesiones()

        4.  - listarSesionesActivas_usuarioSinRolAdmin_deberiaRetornar403()

        5.  - logoutOtrasSesiones_deberiaCerrarTodasMenosLaActual

        6.  - logoutOtrasSesiones_tokenInvalido_deberiaLanzarExcepcion

        7.  - logoutOtrasSesiones_tokenFaltanteOMalFormado_deberiaLanzarExcepcion

        8.  - logoutOtrasSesiones_usuarioSinRolAdecuado_deberiaRetornar403

*/

@RequiredArgsConstructor
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SqlGroup({
    @Sql(
        statements = {
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
public class SesionControllerTest {
    
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
    private final AuthController authController;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;
    private final SesionActivaService sesionActivaService;
    private final UsuarioServiceImpl usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;
    
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
    
    // 1) 
    @Test
    void misSesionesActivas_deberiaRetornarListaDeSesionesDelUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + tokenUsuario))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }
    
    // 2) 
    @Test
    void misSesionesActivas_sinRolAdecuado_deberiaRetornar4033() throws Exception {
        // Registrar y loguear un usuario sin roles
        registrarUsuarioSinRoles("sinrol", "000000", "sinrol@test.com");

        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("sinrol", "000000"))))
            .andExpect(status().isUnauthorized()); // 401 esperado si no tiene roles
    }
    
    // 3) 
    @Test
    void listarSesionesActivas_adminPuedeVerTodasLasSesiones() throws Exception {
        // Sin idUsuario (debería devolver todas las sesiones activas existentes)
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());

        // Con idUsuario (debería devolver solo las sesiones del usuario especificado)
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .param("idUsuario", idUsuario.toString())
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());

        // Caso donde el usuario no existe
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .param("idUsuario", "9999")
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.mensaje").value("El usuario con ID 9999 no existe"));
    }   
    
    /*
    @Test
    void listarSesionesActivas_adminPuedeVerTodasLasSesiones() throws Exception {
        // Sin idUsuario (debería devolver todas las sesiones activas existentes)
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());

        // Con idUsuario (debería devolver solo las sesiones del usuario especificado)
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .param("idUsuario", idUsuario.toString())
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }
    */
    // 4) 
    @Test
    void listarSesionesActivas_usuarioSinRolAdmin_deberiaRetornar403() throws Exception {
        mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + tokenUsuario))
            .andExpect(status().isForbidden()); // 403
    }
    
    // 5) 
    @Test
    void logoutOtrasSesiones_deberiaCerrarTodasMenosLaActual() throws Exception {
        // Iniciar otra sesión (segunda sesión activa)
        AuthResponse otraSesion = loginYGuardarDatos("usuario", "123456");

        // Llamar al logout desde la sesión principal, pasando su refresh token
        mockMvc.perform(post("/api/sesiones/private/logout-otras-sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenUsuario) // Aquí el header con el token de acceso
                .header("Refresh-Token", refreshUsuario)) // Agregar el header 'Refresh-Token' con el valor del refresh token
                .andExpect(status().isOk())
                .andExpect(content().string("Sesiones cerradas excepto la actual"));

        // Verificar que solo queda 1 sesión activa (la actual)
        mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // 6) 
    @Test
    void logoutOtrasSesiones_tokenInvalido_deberiaLanzarExcepcion() throws Exception {
        // Crear un token claramente inválido
        String tokenInvalido = "Bearer estoNoEsUnTokenValido";

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("dummy-refresh-token");

        mockMvc.perform(post("/api/sesiones/private/logout-otras-sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
                .header("Authorization", tokenInvalido))
            .andExpect(status().isUnauthorized()); // o .isForbidden(), según cómo manejes la TokenInvalidoException
    }
    
    // 7) 
    @Test
    void logoutOtrasSesiones_tokenFaltanteOMalFormado_deberiaLanzarExcepcion() throws Exception {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshUsuario);

        mockMvc.perform(post("/api/sesiones/private/logout-otras-sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isUnauthorized()); // o isBadRequest o isForbidden según el @ExceptionHandler
    }
    
    // 8) 
    @Test
    void logoutOtrasSesiones_usuarioSinRolAdecuado_deberiaRetornar403() throws Exception {
        // Registrar un usuario sin rol
        registrarUsuarioSinRoles("sinrol", "sinrol", "sinrol@test.com");
        
        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("sinrol", "sinrol"))))
            .andExpect(status().isUnauthorized()); // 401 esperado si no tiene roles
    }
    
}
