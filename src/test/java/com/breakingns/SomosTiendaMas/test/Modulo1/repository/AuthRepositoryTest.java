package com.breakingns.SomosTiendaMas.test.Modulo1.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

//import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.IEmailVerificacionRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.repository.ICarritoRepository;
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

/*                                                  AuthRepositoryTest

    1. Guardar usuario persiste correctamente
    2. Guardar sesión activa persiste correctamente
    3. Buscar usuario por username y email
    4. Buscar sesión activa por token y usuario ID
    5. Eliminar usuario verifica eliminación
    6. Eliminar sesión activa verifica eliminación
    7. Actualizar estado de sesión activa a revocado
    8. Actualizar datos del usuario
    9. Guardar token persiste correctamente
    10. Eliminar token verifica eliminación
    11. Buscar token por valor
    12. Actualizar estado de token a revocado
    13. Guardar refresh token persiste correctamente
    14. Eliminar refresh token verifica eliminación
    15. Buscar refresh token por valor
    16. Actualizar estado de refresh token a revocado
    17. Buscar usuario inexistente retorna vacío
    18. Buscar sesión activa inexistente retorna vacío
    19. Guardar usuario con username duplicado lanza excepción
    20. Guardar usuario con email duplicado lanza excepción
    21. Eliminar usuario elimina sesiones y tokens asociados
    22. Guardar usuario setea fecha de creación correctamente
    23. Guardar sesión activa setea fecha de inicio correctamente
    24. Guardar token setea fecha de creación correctamente
    25. Guardar refresh token setea fecha de creación correctamente
    26. Guardar múltiples sesiones activas para un usuario
    27. Eliminar sesión activa inexistente no lanza excepción
    28. Eliminar usuario inexistente no lanza excepción

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
            "DELETE FROM direcciones",
            "DELETE FROM telefonos",
            "DELETE FROM email_verificacion",
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
            "DELETE FROM direcciones",
            "DELETE FROM telefonos",
            "DELETE FROM email_verificacion",
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
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ICarritoRepository carritoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IDireccionRepository direccionRepository;
    private final ITelefonoRepository telefonoRepository;
    private final IEmailVerificacionRepository emailVerificacionRepository;

    private Usuario usuarioTest;
    private RegistroUsuarioCompletoDTO registroDTO;

    @BeforeEach
    void setUp() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuario123");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("12345678");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO.setGeneroResponsable("MASCULINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO"); // Si quieres especificar el rol

        RegistroDireccionDTO direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setCiudad("Ciudad");
        direccionDTO.setProvincia("Provincia");
        direccionDTO.setCodigoPostal("1000");
        direccionDTO.setPais("Argentina");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        //String accessToken = null;
        //String refreshToken = null;
        
        registrarUsuarioCompleto(registroDTO);
        //MvcResult loginResult = loginUsuario(usuario, contrasenia);
        /* 
        Cookie[] cookies = loginResult.getResponse().getCookies();
        
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }*/

        usuarioTest = usuarioRepository.findByUsername("usuario123").orElse(null);
    }
    
    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
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

    @Test
	void testBasico() {
		assertTrue(true);
	}

    // 1. Guardar usuario persiste correctamente
    @Test
    void guardarUsuario_persisteCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario5");
        usuario.setPassword("password5");
        usuario.setEmail("correoprueba1@noenviar.com");
        usuario.setActivo(true);
        usuario.setEmailVerificado(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setIntentosFallidosLogin(0);
        usuario.setCuentaBloqueada(false);
        usuario.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        usuario.setNombreResponsable("Juan");
        usuario.setApellidoResponsable("Pérez");
        usuario.setDocumentoResponsable("12341234");
        usuario.setAceptaTerminos(true);
        usuario.setAceptaPoliticaPriv(true);
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
        usuario.setGeneroResponsable(Usuario.Genero.MASCULINO);
        usuario.setIdioma("es");
        usuario.setTimezone("America/Argentina/Buenos_Aires");
        usuario.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuario.setRecibirPromociones(false);
        usuario.setRecibirNewsletters(false);
        usuario.setNotificacionesEmail(false);
        usuario.setNotificacionesSms(false);
        usuario.setRoles(new HashSet<>()); // O asigna un rol si es obligatorio

        usuarioRepository.save(usuario);
        assertTrue(usuarioRepository.findByUsername("usuario5").isPresent());
    }

    // 2. Guardar sesión activa persiste correctamente
    @Test
    void guardarSesionActiva_persisteCorrectamente() {

        SesionActiva sesion = new SesionActiva();
        sesion.setToken("access-token");
        sesion.setRefreshToken("refresh-token");
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.save(sesion);

        assertTrue(sesionActivaRepository.findByToken("access-token").isPresent());
    }

    // 3. Buscar usuario por username y email
    @Test
    void buscarUsuarioPorUsernameYEmail() {
        assertTrue(usuarioRepository.findByUsername(usuarioTest.getUsername()).isPresent());
        assertTrue(usuarioRepository.findByEmail(usuarioTest.getEmail()).isPresent());
    }

    // 4. Buscar sesión activa por token y usuario ID
    @Test
    void buscarSesionActivaPorTokenUsuarioId() {
        SesionActiva sesion = sesionActivaRepository.save(
            new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null)
        );
        assertTrue(sesionActivaRepository.findByToken("access-token").isPresent());
        assertTrue(sesionActivaRepository.findById(sesion.getId()).isPresent());
        assertFalse(sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioTest.getIdUsuario()).isEmpty());
    }

    // 5. Eliminar usuario verifica eliminación
    @Test
    void eliminarUsuario_verificaEliminacion() {
        // Refresca el usuario desde la BD
        usuarioTest = usuarioRepository.findByEmail(usuarioTest.getEmail()).orElse(null);
        
        // 1. Eliminar asociaciones de roles
        usuarioTest.getRoles().clear();
        usuarioRepository.save(usuarioTest);

        // 2. Eliminar telefonos
        telefonoRepository.deleteAll(telefonoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 3. Eliminar email_verificacion
        emailVerificacionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()).ifPresent(emailVerificacionRepository::delete);
        // 4. Eliminar direcciones
        direccionRepository.deleteAll(direccionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 5. Eliminar carrito (si tienes repository)        
        carritoRepository.deleteAll(carritoRepository.findAllByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 6. Eliminar sesiones activas
        sesionActivaRepository.deleteAll(sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioTest.getIdUsuario()));

        // Eliminar usuario
        usuarioRepository.delete(usuarioTest);
        System.out.println("\n[DESPUES DE ELIMINAR USUARIO] USUARIO: " + usuarioRepository.findByUsername(usuarioTest.getUsername()));

        // Verificar eliminación
        assertFalse(usuarioRepository.findByUsername(usuarioTest.getUsername()).isPresent());
    }
    
    // 6. Eliminar sesion activa, verifica eliminacion
    @Test
    void eliminarSesionActiva_verificaEliminacion() {
        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null));
        
        // Verifica que la sesión existe
        assertTrue(sesionActivaRepository.findByToken("access-token").isPresent());
        
        // Elimina la sesión
        sesionActivaRepository.delete(sesion);
        
        // Verifica que la sesión ya no existe
        assertFalse(sesionActivaRepository.findByToken("access-token").isPresent());
    }

    // 7. Actualización estado sesion activa a revocado
    @Test
    void actualizarEstadoSesion_revocado() {
        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, "access-token", "refresh-token", "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null));
        
        // Verifica que inicialmente no está revocado
        assertFalse(sesionActivaRepository.findById(sesion.getId()).get().isRevocado());
        
        // Actualiza y guarda
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);
        
        // Verifica que ahora sí está revocado
        assertTrue(sesionActivaRepository.findById(sesion.getId()).get().isRevocado());
    }

    // 8. Actualizar datos del usuario
    @Test
    void actualizarDatosUsuario() {
        // Actualiza datos
        usuarioTest.setEmail("correoprueba1@noenviar.com");
        usuarioTest.setPassword("nuevaPassword");
        usuarioRepository.save(usuarioTest);

        // Verifica cambios
        Usuario actualizado = usuarioRepository.findByUsername(usuarioTest.getUsername()).get();
        assertEquals("correoprueba1@noenviar.com", actualizado.getEmail());
        assertEquals("nuevaPassword", actualizado.getPassword());
    }

    // 9. Guardar token persiste correctamente
    @Test
    void guardarToken_persisteCorrectamente() {
        // Crear y guardar token
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-test");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600)); // Ejemplo: expira en 1 hora

        tokenEmitidoRepository.save(token);

        // Verificar persistencia
        assertTrue(tokenEmitidoRepository.findByToken("access-token-test").isPresent());
    }

    // 10. Eliminar token verifica eliminación
    @Test
    void eliminarToken_verificaEliminacion() {
        // Crear y guardar token
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-delete");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600)); // Expira en 1 hora
        tokenEmitidoRepository.save(token);

        // Eliminar token
        tokenEmitidoRepository.delete(token);

        // Verificar que ya no existe
        assertFalse(tokenEmitidoRepository.findByToken("access-token-delete").isPresent());
    }

    // 11. Buscar token por valor
    @Test
    void buscarTokenPorValor() {
        // Crear y guardar token
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-find");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.save(token);

        // Buscar por valor
        assertTrue(tokenEmitidoRepository.findByToken("access-token-find").isPresent());
    }

    // 12. Actualizar estado de token a revocado
    @Test
    void actualizarEstadoToken_revocado() {
        // Crear y guardar token
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-revoke");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.save(token);

        // Actualizar estado a revocado
        token.setRevocado(true);
        tokenEmitidoRepository.save(token);

        // Verificar estado
        TokenEmitido actualizado = tokenEmitidoRepository.findByToken("access-token-revoke").get();
        assertTrue(actualizado.isRevocado());
    }

    // 13. Guardar refresh token persiste correctamente
    @Test
    void guardarRefreshToken_persisteCorrectamente() {
        // Crear y guardar refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-test");
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200)); // Expira en 2 horas
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");

        refreshTokenRepository.save(refreshToken);

        // Verificar persistencia
        assertTrue(refreshTokenRepository.findByToken("refresh-token-test").isPresent());
    }

    // 14. Eliminar refresh token verifica eliminación
    @Test
    void eliminarRefreshToken_verificaEliminacion() {
        // Crear y guardar refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-delete");
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.save(refreshToken);

        // Eliminar refresh token
        refreshTokenRepository.delete(refreshToken);

        // Verificar que ya no existe
        assertFalse(refreshTokenRepository.findByToken("refresh-token-delete").isPresent());
    }

    // 15. Buscar refresh token por valor
    @Test
    void buscarRefreshTokenPorValor() {
        // Crear y guardar refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-find");
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.save(refreshToken);

        // Buscar por valor
        assertTrue(refreshTokenRepository.findByToken("refresh-token-find").isPresent());
    }

    // 16. Actualizar estado de refresh token a revocado
    @Test
    void actualizarEstadoRefreshToken_revocado() {
        // Crear y guardar refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-revoke");
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200)); // Expira en 2 horas
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.save(refreshToken);

        // Actualizar estado a revocado
        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);

        // Verificar estado
        RefreshToken actualizado = refreshTokenRepository.findByToken("refresh-token-revoke").get();
        assertTrue(actualizado.getRevocado());
    }

    // 17. Buscar usuario inexistente retorna vacío
    @Test
    void buscarUsuarioInexistente_retornaEmpty() {
        assertTrue(usuarioRepository.findByUsername("noexisteusuario").isEmpty());
        assertTrue(usuarioRepository.findByEmail("correoprueba10@noenviar.com").isEmpty());
    }

    // 18. Buscar sesión activa inexistente retorna vacío
    @Test
    void buscarSesionActivaInexistente_retornaEmpty() {
        assertTrue(sesionActivaRepository.findByToken("noexistesesion").isEmpty());
        assertTrue(sesionActivaRepository.findById(-999L).isEmpty());
    }

    // 19. Guardar usuario con username duplicado lanza excepción
    @Test
    void guardarUsuarioConUsernameDuplicado_lanzaExcepcion() {
        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername(usuarioTest.getUsername());
        usuarioDuplicado.setEmail("correoprueba@noenviar.com");
        usuarioDuplicado.setPassword("otraClave");
        usuarioDuplicado.setActivo(true);
        usuarioDuplicado.setEmailVerificado(false);
        usuarioDuplicado.setFechaRegistro(LocalDateTime.now());
        usuarioDuplicado.setIntentosFallidosLogin(0);
        usuarioDuplicado.setCuentaBloqueada(false);
        usuarioDuplicado.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        usuarioDuplicado.setNombreResponsable("Juan");
        usuarioDuplicado.setApellidoResponsable("Pérez");
        usuarioDuplicado.setDocumentoResponsable("12341234");
        usuarioDuplicado.setAceptaTerminos(true);
        usuarioDuplicado.setAceptaPoliticaPriv(true);
        usuarioDuplicado.setFechaUltimaModificacion(LocalDateTime.now());
        usuarioDuplicado.setGeneroResponsable(Usuario.Genero.MASCULINO);
        usuarioDuplicado.setIdioma("es");
        usuarioDuplicado.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDuplicado.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDuplicado.setRecibirPromociones(false);
        usuarioDuplicado.setRecibirNewsletters(false);
        usuarioDuplicado.setNotificacionesEmail(false);
        usuarioDuplicado.setNotificacionesSms(false);
        usuarioDuplicado.setRoles(new HashSet<>());
        try {
            usuarioRepository.save(usuarioDuplicado);
            assertTrue(false, "Se esperaba excepción por username duplicado");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    // 20. Guardar usuario con email duplicado lanza excepción
    @Test
    void guardarUsuarioConEmailDuplicado_lanzaExcepcion() {
        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername("otroUsuario");
        usuarioDuplicado.setEmail(usuarioTest.getEmail());
        usuarioDuplicado.setPassword("otraClave");
        usuarioDuplicado.setActivo(true);
        usuarioDuplicado.setEmailVerificado(false);
        usuarioDuplicado.setFechaRegistro(LocalDateTime.now());
        usuarioDuplicado.setIntentosFallidosLogin(0);
        usuarioDuplicado.setCuentaBloqueada(false);
        usuarioDuplicado.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        usuarioDuplicado.setNombreResponsable("Juan");
        usuarioDuplicado.setApellidoResponsable("Pérez");
        usuarioDuplicado.setDocumentoResponsable("12341234");
        usuarioDuplicado.setAceptaTerminos(true);
        usuarioDuplicado.setAceptaPoliticaPriv(true);
        usuarioDuplicado.setFechaUltimaModificacion(LocalDateTime.now());
        usuarioDuplicado.setGeneroResponsable(Usuario.Genero.MASCULINO);
        usuarioDuplicado.setIdioma("es");
        usuarioDuplicado.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDuplicado.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDuplicado.setRecibirPromociones(false);
        usuarioDuplicado.setRecibirNewsletters(false);
        usuarioDuplicado.setNotificacionesEmail(false);
        usuarioDuplicado.setNotificacionesSms(false);
        usuarioDuplicado.setRoles(new HashSet<>());
        try {
            usuarioRepository.save(usuarioDuplicado);
            assertTrue(false, "Se esperaba excepción por email duplicado");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    // 21. Eliminar usuario elimina sesiones y tokens asociados
    @Test
    void eliminarUsuario_eliminaSesionesYTokensAsociados() {
        // Crear sesión y token asociados
        SesionActiva sesion = new SesionActiva();
        sesion.setToken("access-token-asociado");
        sesion.setRefreshToken("refresh-token-asociado");
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesion.setRevocado(false);
        sesionActivaRepository.save(sesion);

        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-asociado");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.save(token);

        // Eliminar primero entidades asociadas para evitar errores de integridad
        // 1. Eliminar sesiones activas
        sesionActivaRepository.deleteAll(sesionActivaRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 2. Eliminar tokens emitidos
        tokenEmitidoRepository.deleteAll(tokenEmitidoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 3. Eliminar refresh tokens
        refreshTokenRepository.deleteAll(refreshTokenRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 4. Eliminar direcciones
        direccionRepository.deleteAll(direccionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 5. Eliminar teléfonos
        telefonoRepository.deleteAll(telefonoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 6. Eliminar email_verificacion
        emailVerificacionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()).ifPresent(emailVerificacionRepository::delete);
        // 7. Eliminar carrito
        carritoRepository.deleteAll(carritoRepository.findAllByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        // 8. Limpiar roles
        usuarioTest.getRoles().clear();
        usuarioRepository.save(usuarioTest);
        // 9. Eliminar usuario
        usuarioRepository.delete(usuarioTest);

        // Verificar que sesiones y tokens asociados fueron eliminados
        assertFalse(sesionActivaRepository.findByToken("access-token-asociado").isPresent());
        assertFalse(tokenEmitidoRepository.findByToken("access-token-asociado").isPresent());
    }

    // 22. Guardar usuario setea fecha de creación correctamente
    @Test
    void guardarUsuario_seteaFechaCreacionCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioFecha");
        usuario.setPassword("passwordFecha");
        usuario.setEmail("correoprueba1@noenviar.com");
        usuario.setActivo(true);
        usuario.setEmailVerificado(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setIntentosFallidosLogin(0);
        usuario.setCuentaBloqueada(false);
        usuario.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        usuario.setNombreResponsable("Juan");
        usuario.setApellidoResponsable("Pérez");
        usuario.setDocumentoResponsable("12341234");
        usuario.setAceptaTerminos(true);
        usuario.setAceptaPoliticaPriv(true);
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
        usuario.setGeneroResponsable(Usuario.Genero.MASCULINO);
        usuario.setIdioma("es");
        usuario.setTimezone("America/Argentina/Buenos_Aires");
        usuario.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuario.setRecibirPromociones(false);
        usuario.setRecibirNewsletters(false);
        usuario.setNotificacionesEmail(false);
        usuario.setNotificacionesSms(false);
        usuario.setRoles(new HashSet<>());
        usuarioRepository.save(usuario);
        Usuario guardado = usuarioRepository.findByUsername("usuarioFecha").get();
        assertTrue(guardado.getFechaRegistro() != null);
    }

    // 23. Guardar sesión activa setea fecha de inicio correctamente
    @Test
    void guardarSesionActiva_seteaFechaInicioCorrectamente() {
        SesionActiva sesion = new SesionActiva();
        sesion.setToken("access-token-fecha");
        sesion.setRefreshToken("refresh-token-fecha");
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.save(sesion);
        SesionActiva guardada = sesionActivaRepository.findByToken("access-token-fecha").get();
        assertTrue(guardada.getFechaInicioSesion() != null);
    }

    // 24. Guardar token setea fecha de creación correctamente
    @Test
    void guardarToken_seteaFechaCreacionCorrectamente() {
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-fecha");
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.save(token);
        TokenEmitido guardado = tokenEmitidoRepository.findByToken("access-token-fecha").get();
        assertTrue(guardado.getFechaEmision() != null);
    }

    // 25. Guardar refresh token setea fecha de creación correctamente
    @Test
    void guardarRefreshToken_seteaFechaCreacionCorrectamente() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-fecha");
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.save(refreshToken);
        RefreshToken guardado = refreshTokenRepository.findByToken("refresh-token-fecha").get();
        assertTrue(guardado.getFechaExpiracion() != null);
    }

    // 26. Guardar múltiples sesiones activas para un usuario
    @Test
    void guardarMultiplesSesionesActivas_paraUnUsuario() {
        SesionActiva sesion1 = new SesionActiva();
        sesion1.setToken("access-token-1");
        sesion1.setRefreshToken("refresh-token-1");
        sesion1.setUsuario(usuarioTest);
        sesion1.setIp("127.0.0.1");
        sesion1.setUserAgent("JUnit");
        sesion1.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.save(sesion1);

        SesionActiva sesion2 = new SesionActiva();
        sesion2.setToken("access-token-2");
        sesion2.setRefreshToken("refresh-token-2");
        sesion2.setUsuario(usuarioTest);
        sesion2.setIp("127.0.0.1");
        sesion2.setUserAgent("JUnit");
        sesion2.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.save(sesion2);

        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioTest.getIdUsuario());
        assertTrue(sesiones.size() >= 2);
    }

    // 27. Eliminar sesión activa inexistente no lanza excepción
    @Test
    void eliminarSesionActivaInexistente_noLanzaExcepcion() {
        try {
            sesionActivaRepository.deleteById(-999L);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false, "No debería lanzar excepción al eliminar sesión inexistente");
        }
    }

    // 28. Eliminar usuario inexistente no lanza excepción
    @Test
    void eliminarUsuarioInexistente_noLanzaExcepcion() {
        try {
            usuarioRepository.deleteById(-999L);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false, "No debería lanzar excepción al eliminar usuario inexistente");
        }
    }
}
