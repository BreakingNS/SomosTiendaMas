package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioService;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.RefreshTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
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

@Sql(statements = {
    "DELETE FROM tokens_reset_password",
    "DELETE FROM sesiones_activas",
    "DELETE FROM token_emitido",
    "DELETE FROM refresh_token",
    "DELETE FROM carrito",
    "DELETE FROM refresh_token",
    "DELETE FROM usuario_roles",
    "DELETE FROM usuario"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@RequiredArgsConstructor
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthIntegrationTest {
    
    /*      Metodos:
        
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
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;
    
    private String refreshAdmin;
    private String refreshUsuario;
    private String tokenAdmin;
    private String tokenUsuario;
    private Long idAdmin;
    private Long idUsuario;
    
    @BeforeAll
    void setUp() throws Exception {
        
        // Configuración inicial antes de las pruebas
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword("987654");
        admin.setEmail("admin@test.com");

        // Verifica si la ruta pública es accesible en el test
        mockMvc.perform(post("/api/registro/public/admin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(admin)))
        .andExpect(status().isOk())
        .andExpect(content().string("Administrador registrado correctamente"));
        
        // Login del admin
        LoginRequest loginAdmin = new LoginRequest("admin", "987654");

        String responseAdmin = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginAdmin))
                .header("User-Agent", "MockMvc") // 🛠️ importante
                .with(request -> {
                    request.setRemoteAddr("127.0.0.1"); // 🛠️ importante
                    return request;
                }))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        AuthResponse jwtResponseAdmin = objectMapper.readValue(responseAdmin, AuthResponse.class);
        
        tokenAdmin = jwtResponseAdmin.accessToken();
        refreshAdmin = jwtResponseAdmin.refreshToken();
        idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        
        // Configuración inicial antes de las pruebas
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario");
        usuario.setPassword("123456");
        usuario.setEmail("usuario@test.com");

        // Verifica si la ruta pública es accesible en el test
        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
        .andExpect(status().isOk())
        .andExpect(content().string("Usuario registrado correctamente"));
        
        // Login del usuario
        LoginRequest loginUsuario = new LoginRequest("usuario", "123456");

        String responseUsuario = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUsuario))
                .header("User-Agent", "MockMvc") // 🛠️ importante
                .with(request -> {
                    request.setRemoteAddr("127.0.0.2"); // 🛠️ importante
                    return request;
                }))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        AuthResponse jwtResponseUsuario = objectMapper.readValue(responseUsuario, AuthResponse.class);
        
        tokenUsuario = jwtResponseUsuario.accessToken();
        refreshUsuario = jwtResponseUsuario.refreshToken();
        idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
        
    }
    
    // Verifica que un usuario cuando se registra, si existe en la BD
    @Test
    void registroUsuario_deberiaCrearUsuario() throws Exception {
        System.out.println("Ingreso a Test: registroUsuario_deberiaCrearUsuario..");
        
        assertFalse(usuarioService.existeUsuario("usuarioPrueba"));
        
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioPrueba");
        usuario.setPassword("abcdef");
        usuario.setEmail("usuario2@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
        .andExpect(status().isOk())
        .andExpect(content().string("Usuario registrado correctamente"));
        
        assertTrue(usuarioService.existeUsuario("usuarioPrueba"));
        
        System.out.println("Salida de Test: registroUsuario_deberiaCrearUsuario");
    }
    
    // Verifica que se generaron correctamente los tokens
    @Test
    void loginGeneraTokensCorrectamente() {
        System.out.println("Ingreso a Test: loginGeneraTokensCorrectamente");
        
        
        assertNotNull(tokenAdmin);
        assertFalse(tokenAdmin.isBlank());
        assertNotNull(tokenUsuario);
        assertFalse(tokenUsuario.isBlank());

        System.out.println("Token admin: " + tokenAdmin);
        System.out.println("Token user: " + tokenUsuario);
        
        System.out.println("Salida de Test: loginGeneraTokensCorrectamente");
    }

    //Acceso a Carrito de Usuario por Admin (Autorizado)
    @Test
    void accesoACarritoUaA() throws Exception {
        System.out.println("Ingreso a Test: accesoACarritoUaA");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
        
        System.out.println("Salida de Test: accesoACarritoUaA");
    }
    
    //Acceso a Carrito de Admin por Usuario (No Autorizado)
    @Test
    void accesoACarritoAaU() throws Exception {
        System.out.println("Ingreso a Test: accesoACarritoAaU");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
        
        System.out.println("Salida de Test: accesoACarritoAaU");
    }
    
    //Acceso a Carrito de Admin por Admin (Autorizado)
    @Test
    void accesoACarritoAaA() throws Exception {
        System.out.println("Ingreso a Test: accesoACarritoAaA");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    
        System.out.println("Salida de Test: accesoACarritoAaA");
    }
    
    //Acceso a Carrito de Usuario por Usuario (Autorizado)
    @Test
    void accesoACarritoUaU() throws Exception {
        System.out.println("Ingreso a Test: accesoACarritoUaU");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());
        
        System.out.println("Salida de Test: accesoACarritoUaU");
    }
    
    // Accede a ruta protegida sin token
    @Test
    void accesoARutaProtegidaSinToken() throws Exception {
        System.out.println("Ingreso a Test: accesoARutaProtegidaSinToken");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario))
                .andExpect(status().isUnauthorized());
        
        System.out.println("Salida de Test: accesoARutaProtegidaSinToken");
    }
    
    // Accede a ruta inexistente
    @Test
    void accesoARutaInexistente() throws Exception {
        System.out.println("Ingreso a Test: accesoARutaInexistente");
        
        
        mockMvc.perform(get("/api/algunaruta/traera/")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isNotFound());
    
        System.out.println("Salida de Test: accesoARutaInexistente");
    }
    
    // Accede a ruta protegida con token invalido
    @Test
    void accesoARutaProtegidaConTokenInvalido() throws Exception {
        System.out.println("Ingreso a Test: accesoARutaProtegidaConTokenInvalido");
        
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario + "a"))
                .andExpect(status().isForbidden());
        
        System.out.println("Salida de Test: accesoARutaProtegidaConTokenInvalido");
    }
    
    // Login con credenciales incorrectas
    @Test
    void loginIncorrecto() throws Exception {
        System.out.println("Ingreso a Test: loginIncorrecto");
        
        
        // Login del usuario
        LoginRequest loginUsuario = new LoginRequest("usuario", "holamundo");

        String responseUsuario = mockMvc.perform(post("/api/auth/public/login")
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
    
        System.out.println("Salida de Test: loginIncorrecto");
    }
    
    // Registro con campos invalidos
    @Test
    void registroConCamposInvalidos() throws Exception {
        System.out.println("Ingreso a Test: registroConCamposInvalidos");
        
        
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
        
        System.out.println("Salida de Test: registroConCamposInvalidos");
    }
    
    //Logout simple, un solo logout
    @Test
    void logoutSimple() throws Exception {
        System.out.println("Ingreso a Test: logoutSimple");
        
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
            .andExpect(jsonPath("$.message").value("Sesión cerrada correctamente"));
        System.out.println("Supuesto logout terminado");
        
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
        
        System.out.println("Salida de Test: logoutSimple");
    }
    // 🚧 A futuro: refresh token y logout
    
}
/*      Metodos:
        
        status().isOk() → 200
        status().isUnauthorized() → 401
        status().isForbidden() → 403
        status().isInternalServerError() → 500
        status().isBadRequest() → 400
        status().isCreated() → 201
        status().isNotFound() → 404
        status().isNoContent() → 204
    
    */