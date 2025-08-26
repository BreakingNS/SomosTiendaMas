package com.breakingns.SomosTiendaMas.test.Modulo2.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.Instant;

import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.SqlGroup;

/*                                                  AuthModelTest
    Validaciones de Entidades

        1. Usuario con caracteres especiales no permitidos (400)
        2. Usuario demasiado corto (<6) o demasiado largo (>16) (400)
        3. Usuario con doble punto o doble guion bajo (400)
        4. Contraseña sin números (400)
        5. Contraseña sin letras (400)
        6. Contraseña con espacios (400)
        7. Contraseña igual al nombre de usuario (400)
        8. Email inválido (sin @, sin dominio, etc.) (400)
        9. Usuario ya registrado (400)
        10. Email ya registrado (400)
        11. Usuario válido con caracteres permitidos (guion bajo, punto) (200)
        12. Contraseña válida con caracteres especiales permitidos (200)
        13. Registro con campos vacíos (400)
        14. Registro con cantidad minima (6) y maxima (16) de caracteres (200)
        15. Registro con todos los datos válidos (200)
        16. Registro, teniendo contraseña con caracteres especiales (200)
        17. Username con guion medio permitido (200)
        18. Username empieza o termina con punto, guion bajo o guion medio (400)
        19. Username con secuencias ._ o -.
        20. Contraseña con solo caracteres especiales (400)
        21. Username con espacios al inicio o final (400)
        22. Email con espacios o caracteres no permitidos (400)
        23. Username con mayúsculas y minúsculas (200)
        24. Contraseña con longitud exactamente en los límites (200)
        25. Campos nulos (400)

            26. Username con caracteres Unicode (400/200)
            27. Email con subdominios (200)
            28. Email con caracteres especiales permitidos (200)
            29. Username con caracteres repetidos permitidos (200/400)
    
    Validación de Token (expiración, formato, unicidad)

        30. Token con formato inválido (400)
        31. Token expirado (401)
        32. Token duplicado (400)

            33.Token firmado con clave incorrecta (401)
            34. Token con claims faltantes (400)
            35. Token con fecha de expiración en el pasado (401)
            36. Token con fecha de expiración en el futuro pero revocado (401)

    Validación de Sesión Activa
    
        37. Sesión activa válida (200)    
        38. Sesión activa expirada (401)
        39. Sesión activa inexistente (404)

            40.Sesión activa con usuario eliminado (404)
            41. Sesión activa con token revocado (401)

    Negocio de revocación y logout

        42. Logout exitoso (200)
        43. Logout con token inválido (401)

            44. Logout con refresh token en vez de access (400)
            45. Logout con token ya revocado (400/401)
            46. Logout con sesión expirada (401)

    Reglas de negocio en los modelos 
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
class AuthRepositoryTest {

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

    private final IUsuarioRepository usuarioRepository;
    //private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    //private final IRefreshTokenRepository refreshTokenRepository;
    
    @BeforeEach
    void setUp() throws Exception{
        
        String usuario = "usuPrueba";
        String contrasenia = "P123456";
        //String accessToken = null;
        //String refreshToken = null;
        
        registrarUsuario(usuario, contrasenia, "usuPrueba@prueba.com");
        /*MvcResult loginResult = loginUsuario(usuario, contrasenia);
        
        Cookie[] cookies = loginResult.getResponse().getCookies();
        
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }*/
    }

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
    /*
    // Método para hacer login de un usuario
    private MvcResult loginUsuario(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc"))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult;
    }*/

    // 1. Persistencia

    @Test
    void guardarUsuario_persisteCorrectamente() {
        Usuario usuario = new Usuario(null, "usuario", "password", "prueba@test.com", null);
        usuarioRepository.save(usuario);
        assertTrue(usuarioRepository.findByUsername("usuario").isPresent());
    }
    /*
    @Test
    void guardarToken_persisteCorrectamente() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token");
        token.setUsuario(usuario);
        tokenEmitidoRepository.save(token);
        assertTrue(tokenEmitidoRepository.findByToken("access-token").isPresent());
    }*/

    @Test
    void guardarSesionActiva_persisteCorrectamente() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        SesionActiva sesion = new SesionActiva();
        sesion.setToken("access-token");
        sesion.setRefreshToken("refresh-token");
        sesion.setUsuario(usuario);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.save(sesion);
        assertTrue(sesionActivaRepository.findByToken("access-token").isPresent());
    }

    // 2. Consulta

    @Test
    void buscarUsuarioPorUsernameYEmail() {
        usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        assertTrue(usuarioRepository.findByUsername("usuario").isPresent());
        assertTrue(usuarioRepository.findByEmail("prueba@test.com").isPresent());
    }
    /*
    @Test
    void buscarTokenPorValor() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        tokenEmitidoRepository.save(new TokenEmitido("access-token", usuario));
        refreshTokenRepository.save(new RefreshToken("refresh-token", usuario));
        assertTrue(tokenEmitidoRepository.findByToken("access-token").isPresent());
        assertTrue(refreshTokenRepository.findByToken("refresh-token").isPresent());
    }
    */
    @Test
    void buscarSesionActivaPorTokenUsuarioId() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuario, Instant.now(), null));
        assertTrue(sesionActivaRepository.findByToken("access-token").isPresent());
        assertTrue(sesionActivaRepository.findById(sesion.getId()).isPresent());
        assertFalse(sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario()).isEmpty());
    }

    // 3. Eliminación

    @Test
    void eliminarUsuario_verificaEliminacion() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        usuarioRepository.delete(usuario);
        assertFalse(usuarioRepository.findByUsername("usuario").isPresent());
    }
    /* 
    @Test
    void eliminarToken_verificaEliminacion() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        TokenEmitido token = tokenEmitidoRepository.save(new TokenEmitido("access-token", usuario));
        tokenEmitidoRepository.delete(token);
        assertFalse(tokenEmitidoRepository.findByToken("access-token").isPresent());
    }
    */
    @Test
    void eliminarSesionActiva_verificaEliminacion() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuario, Instant.now(), null));
        sesionActivaRepository.delete(sesion);
        assertFalse(sesionActivaRepository.findByToken("access-token").isPresent());
    }

    // 4. Actualización

    @Test
    void actualizarEstadoSesion_revocado() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuario, Instant.now(), null));
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);
        assertTrue(sesionActivaRepository.findById(sesion.getId()).get().isRevocado());
    }
    /* 
    @Test
    void actualizarEstadoToken_revocado() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        TokenEmitido token = tokenEmitidoRepository.save(new TokenEmitido("access-token", usuario));
        token.setRevocado(true);
        tokenEmitidoRepository.save(token);
        assertTrue(tokenEmitidoRepository.findByToken("access-token").get().isRevocado());
    }
    */
    @Test
    void actualizarDatosUsuario() {
        Usuario usuario = usuarioRepository.save(new Usuario(null, "usuario", "password", "prueba@test.com", null));
        usuario.setEmail("nuevo@test.com");
        usuario.setPassword("nuevaPassword");
        usuarioRepository.save(usuario);
        Usuario actualizado = usuarioRepository.findByUsername("usuario").get();
        assertEquals("nuevo@test.com", actualizado.getEmail());
        assertEquals("nuevaPassword", actualizado.getPassword());
    }
}
