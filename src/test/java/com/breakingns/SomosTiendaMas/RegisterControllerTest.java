package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.controller.RegistroController;
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
import com.breakingns.SomosTiendaMas.domain.usuario.service.IUsuarioService;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import lombok.RequiredArgsConstructor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/*
                                            RegisterControllerTest

        1. Registro de usuario valido - registroUsuarioValido()

        2. Registro con email invalido - registroConEmailInvalido()

        3. Registro con nombre vacio - registroConNombreVacio()

        4. Registro con password corto - registroConPasswordCorta()

        5. Registro con password largo - registroConPasswordLarga()

        6. Registro con email repetido - registroConEmailRepetido()

        7. Registro sin rol asignado desde Service - registroSinRolAsignadoDesdeService()

        8. Registro con RequestBody Invalido - registroConRequestBodyInvalido()

        9. Registro lanza excepcion interna - registroLanzaExcepcionInterna()

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
public class RegisterControllerTest {
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
    
    @Mock  // Mockeamos el servicio
    private UsuarioServiceImpl usuarioService;
    
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthController authController;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;
    private final SesionActivaService sesionActivaService;
    //private final UsuarioServiceImpl usuarioService;
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
    
    // 1) Registro de usuario valido
    @Test
    void registroUsuarioValido() throws Exception {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername("nuevoUser");
        nuevoUsuario.setPassword("claveSegura123");
        nuevoUsuario.setEmail("nuevo@correo.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoUsuario)))
            .andExpect(status().isOk())
            .andExpect(content().string("Usuario registrado correctamente"));
    }
    
    // 2) Registro con email invalido
    @Test
    void registroConEmailInvalido() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioEmailInvalido");
        usuario.setPassword("claveSegura123");
        usuario.setEmail("correo-invalido"); // sin @ ni dominio

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value("El correo electrónico no tiene un formato válido"));
    }
    
    // 3) Registro con nombre vacio
    @Test
    void registroConNombreVacio() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(""); // vacío
        usuario.setPassword("claveSegura123");
        usuario.setEmail("email@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value("El nombre de usuario no puede estar vacío")); // ajustá según tu mensaje
    }
    
    // 4) Registro con password corto
    @Test
    void registroConPasswordCorta() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("nuevoUsuario");
        usuario.setPassword("123"); // demasiado corta
        usuario.setEmail("correo@valido.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres."));
    }
    
    // 5) Registro con password largo
    @Test
    void registroConPasswordLarga() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioConPasswordLarga");
        usuario.setPassword("contraseniamuylargaparaesteregistro".repeat(100)); // demasiado larga
        usuario.setEmail("valido@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.")); // ajustá el mensaje si es necesario
    }
    
    // 6) Registro con email repetido
    @Test
    void registroConEmailRepetido() throws Exception {
        // Primero registrar un usuario con ese email
        Usuario usuarioOriginal = new Usuario();
        usuarioOriginal.setUsername("usuarioOriginal");
        usuarioOriginal.setPassword("clave123");
        usuarioOriginal.setEmail("repetido@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioOriginal)))
            .andExpect(status().isOk());

        // Intentar registrar otro con el mismo email
        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername("otroUsuario");
        usuarioDuplicado.setPassword("clave456");
        usuarioDuplicado.setEmail("repetido@test.com"); // mismo email

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioDuplicado)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value("El correo electrónico ya está en uso")); // ajustá el mensaje según tu implementación
    }
    
    // 7) Registro sin rol asignado desde Service
    @Test
    void registroSinRolAsignadoDesdeService() throws Exception {
        // Crear el usuario sin rol asignado
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setUsername("usuarioSinRol");
        usuarioSinRol.setPassword("clave123");
        usuarioSinRol.setEmail("usuarioSinRol@test.com");

        // Realizar el registro sin rol
        mockMvc.perform(post("/api/registro/public/sinrol")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioSinRol)))
            .andExpect(status().isOk());

        // Verificar que el usuario fue creado correctamente sin rol
        // Asegurarse de que el rol no esté asignado
        Usuario usuarioGuardado = usuarioRepository.findByUsername("usuarioSinRol").orElse(null);
        assertNotNull(usuarioGuardado);
        assertTrue(usuarioGuardado.getRoles().isEmpty()); // Verificar que no tenga roles asignados
    }
    
    // 8) Registro con RequestBody Invalido
    @Test
    void registroConRequestBodyInvalido() throws Exception {
        // Enviar un RequestBody inválido (por ejemplo, con campos nulos)
        String requestBodyInvalido = "{}"; // RequestBody vacío o incompleto

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBodyInvalido))
            .andExpect(status().isBadRequest()) // Esperamos un error 400 (Bad Request)
            .andExpect(jsonPath("$.mensaje").value("El nombre de usuario no puede estar vacío"));
    }
    
    // 9) Registro lanza excepcion interna
    @Test
        void registroLanzaExcepcionInterna() throws Exception {
        // Simulamos una excepción interna en el servicio
        doThrow(new RuntimeException("Error interno en el servidor"))
            .when(usuarioService).registrarConRol(any(Usuario.class), any(RolNombre.class));

        // Preparar el cuerpo de la solicitud con un usuario que provocará la excepción
        Usuario usuario = new Usuario();
        usuario.setUsername("forzar-error");
        usuario.setPassword("123456");
        usuario.setEmail("error@prueba.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isInternalServerError()) // Esperamos un error 500
            .andExpect(jsonPath("$.mensaje").value("Error interno en el servidor")); // Verificamos que el mensaje sea correcto
    }
    
}

