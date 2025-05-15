package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.model.Carrito;
import com.breakingns.SomosTiendaMas.repository.ICarritoRepository;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;
import static org.mockito.Mockito.when;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@ActiveProfiles("test")
public class DatabaseConnectionTest {
        
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    
    private final AuthController authController;
    
    private final RolService rolService;
    private final CarritoService carritoService;
    private final UsuarioServiceImpl usuarioService;
    
    private final IUsuarioRepository usuarioRepository;
    private final ICarritoRepository carritoRepository;
    //private final ITokenEmitidoRepository tokenEmitidoRepository;
    
    @MockBean
    private ITokenEmitidoRepository tokenEmitidoRepository;
    
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

    // 1) Error de conexión: la base de datos no está disponible
    @Test
    void testDatabaseConnectionError() {
        Throwable thrown = assertThrows(DataAccessException.class, () -> {
            jdbcTemplate.execute("SELECT * FROM tabla_que_no_existe");
        });

        assertTrue(thrown.getMessage().contains("tabla_que_no_existe")); // opcional: validación del mensaje
    }
    
    // 2) Error de ejecución SQL: tabla inexistente
    @Test
    void testQueryInvalidTable() {
        Throwable thrown = assertThrows(DataAccessException.class, () -> {
            jdbcTemplate.execute("SELECT * FROM tabla_que_no_existe");
        });

        assertTrue(thrown.getMessage().contains("tabla_que_no_existe"));
    }
    
    // 3) Violación de clave única (Primary Key / Unique Constraint)
    @Test
    void testDuplicateEmailRegistration() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario1");
        usuario.setPassword("1234567");
        usuario.setEmail("usu@usu.com");

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk());

        Usuario duplicado = new Usuario();
        duplicado.setUsername("usuario2");
        duplicado.setPassword("1234569");
        duplicado.setEmail("usu@usu.com"); // Mismo email

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El correo electrónico ya está en uso")));
    }

    // 4) Violación de una restricción NOT NULL
    @Test
    void testNullEmailValidation() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario1");
        usuario.setPassword("1234567");
        usuario.setEmail(null); // Campo obligatorio

        mockMvc.perform(post("/api/registro/public/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isBadRequest()) // Esperamos 400 por validación fallida
            .andExpect(content().string(org.hamcrest.Matchers.containsString("El correo electrónico no puede estar vacío"))); // Mensaje de error
    }
    
    // 5) Timeout de conexión
    @Test
    void testConnectionTimeout() {
        assertThrows(DataAccessResourceFailureException.class, () -> {
            // Configuración de un DataSource incorrecto para simular un fallo de conexión
            DataSource dataSource = (DataSource) DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/incorrect_db") // DB incorrecta
                    .username("incorrect_user")
                    .password("incorrect_password")
                    .driverClassName("org.postgresql.Driver")
                    .build();

            JdbcTemplate jdbcTemplate = new JdbcTemplate((javax.sql.DataSource) dataSource);
            jdbcTemplate.execute("SELECT 1"); // Intentar ejecutar la consulta que fallará
        });
    }
    
    // 6) Error de autenticación: usuario o contraseña inválida
    @Test
    void testAuthenticationFailure() {
        assertThrows(DataAccessException.class, () -> {
            // Configuración de un DataSource con credenciales incorrectas para simular un fallo de autenticación
            DataSource dataSource = (DataSource) DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/somostiendamas") // DB correcta
                    .username("incorrect_user") // Usuario incorrecto
                    .password("incorrect_password") // Contraseña incorrecta
                    .driverClassName("org.postgresql.Driver")
                    .build();

            JdbcTemplate jdbcTemplate = new JdbcTemplate((javax.sql.DataSource) dataSource);
            jdbcTemplate.execute("SELECT 1"); // Intentar ejecutar la consulta, que debería fallar por autenticación
        });
    }
    
    // 7) Simular caída durante una transacción
    @Test
    @Transactional
    void testTransactionRollbackPorUsuarioInexistente() {
        Usuario usuario = new Usuario(999L, "hola", "mundo123", "ej@ej.com", null);
        Carrito carrito = new Carrito(); // o new Carrito(usuario), si tenés constructor
        carrito.setUsuario(usuario);
        usuario.setCarrito(carrito);

        // Esperamos que explote al intentar guardar carrito con usuario no persistido
        DataIntegrityViolationException assertThrows = assertThrows(DataIntegrityViolationException.class, () -> {
            carritoRepository.saveAndFlush(carrito);
        });

    }
    
}