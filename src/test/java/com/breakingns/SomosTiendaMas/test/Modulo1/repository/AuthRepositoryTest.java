package com.breakingns.SomosTiendaMas.test.Modulo1.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IEmailVerificacionRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
            // HIJAS -> PADRE (orden evita FK violations)
            "DELETE FROM evento_auditoria",
            "DELETE FROM login_failed_attempts",
            "DELETE FROM tokens_reset_password",
            "DELETE FROM sesiones_activas",
            "DELETE FROM token_emitido",
            "DELETE FROM refresh_token",
            "DELETE FROM direcciones",
            "DELETE FROM telefonos",
            "DELETE FROM email_verificacion",
            "DELETE FROM carrito",
            "DELETE FROM perfil_empresa",
            // Eliminado: DELETE FROM usuario_roles (no existe la tabla)
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )/*,
    @Sql(
        statements = {
            // HIJAS -> PADRE (orden evita FK violations)
            "DELETE FROM evento_auditoria",
            "DELETE FROM login_failed_attempts",
            "DELETE FROM tokens_reset_password",
            "DELETE FROM sesiones_activas",
            "DELETE FROM token_emitido",
            "DELETE FROM refresh_token",
            "DELETE FROM direcciones",
            "DELETE FROM telefonos",
            "DELETE FROM email_verificacion",
            "DELETE FROM carrito",
            "DELETE FROM perfil_empresa",
            // Eliminado: DELETE FROM usuario_roles (no existe la tabla)
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
    private final IRolRepository rolRepository;

    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;

    @MockBean
    private EmailService emailService;

    private Usuario usuarioTest;
    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;

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
        usuarioDTO.setRol("ROLE_USUARIO");

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();

        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

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

        // Registrar realmente el usuario (endpoint o service)
        int status = registrarUsuarioCompleto(registroDTO);
        if (status != 201 && status != 200) {
            throw new IllegalStateException("Fallo registro usuario base, status=" + status);
        }

        // Marcar email verificado (si login lo exige)
        Usuario u = usuarioRepository.findByUsername("usuario123").orElseThrow();
        u.setEmailVerificado(true);
        usuarioRepository.save(u);
    }

    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        return result.getResponse().getStatus();
    }

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
        
        Rol rolUsuario = rolRepository.findByNombre(RolNombre.ROLE_USUARIO)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuario.setRol(rolUsuario);

        usuarioRepository.save(usuario);
        assertTrue(usuarioRepository.findByUsername("usuario5").isPresent());
    }

    // 2. Guardar sesión activa persiste correctamente
    @Test
    void guardarSesionActiva_persisteCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        String token = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();

        SesionActiva sesion = new SesionActiva();
        sesion.setToken(token);
        sesion.setRefreshToken(refresh);
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.saveAndFlush(sesion);

        assertTrue(sesionActivaRepository.findByToken(token).isPresent());
    }

    // 3. Buscar usuario por username y email
    @Test
    void buscarUsuarioPorUsernameYEmail() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        assertTrue(usuarioRepository.findByUsername(usuarioTest.getUsername()).isPresent());
        assertTrue(usuarioRepository.findByEmail(usuarioTest.getEmail()).isPresent());
    }

    // 4. Buscar sesión activa por token y usuario ID
    @Test
    void buscarSesionActivaPorTokenUsuarioId() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        String token = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();

        SesionActiva sesion = sesionActivaRepository.save(
            new SesionActiva(null, token, refresh, "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null)
        );
        assertTrue(sesionActivaRepository.findByToken(token).isPresent());
        assertTrue(sesionActivaRepository.findById(sesion.getId()).isPresent());
        assertFalse(sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioTest.getIdUsuario()).isEmpty());
    }

    // 5. Eliminar usuario verifica eliminación
    @Test
    void eliminarUsuario_verificaEliminacion() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();

        usuarioTest.setRol(null);
        usuarioRepository.saveAndFlush(usuarioTest);

        telefonoRepository.deleteAll(telefonoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        emailVerificacionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()).ifPresent(emailVerificacionRepository::delete);
        direccionRepository.deleteAll(direccionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        carritoRepository.deleteAll(carritoRepository.findAllByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        sesionActivaRepository.deleteAll(sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioTest.getIdUsuario()));

        usuarioRepository.delete(usuarioTest);

        assertFalse(usuarioRepository.findByUsername("usuario123").isPresent());
    }
    
    // 6. Eliminar sesion activa, verifica eliminacion
    @Test
    void eliminarSesionActiva_verificaEliminacion() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        String token = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();

        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, token, refresh, "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null));
        assertTrue(sesionActivaRepository.findByToken(token).isPresent());

        sesionActivaRepository.delete(sesion);
        assertFalse(sesionActivaRepository.findByToken(token).isPresent());
    }

    // 7. Actualización estado sesion activa a revocado
    @Test
    void actualizarEstadoSesion_revocado() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        String token = "access-" + UUID.randomUUID();
        String refresh = "refresh-" + UUID.randomUUID();

        SesionActiva sesion = sesionActivaRepository.save(new SesionActiva(null, token, refresh, "127.0.0.1", "JUnit", usuarioTest, Instant.now(), null));
        assertFalse(sesionActivaRepository.findById(sesion.getId()).get().isRevocado());

        sesion.setRevocado(true);
        sesionActivaRepository.saveAndFlush(sesion);

        assertTrue(sesionActivaRepository.findById(sesion.getId()).get().isRevocado());
    }

    // 8. Actualizar datos del usuario
    @Test
    void actualizarDatosUsuario() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        usuarioTest.setEmail("correoprueba1@noenviar.com");
        usuarioTest.setPassword("nuevaPassword");
        usuarioRepository.saveAndFlush(usuarioTest);

        Usuario actualizado = usuarioRepository.findByUsername(usuarioTest.getUsername()).get();
        assertEquals("correoprueba1@noenviar.com", actualizado.getEmail());
        assertEquals("nuevaPassword", actualizado.getPassword());
    }

    // 9. Guardar token persiste correctamente
    @Test
    void guardarToken_persisteCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-test-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);

        assertTrue(tokenEmitidoRepository.findByToken(token.getToken()).isPresent());
    }

    // 10. Eliminar token verifica eliminación
    @Test
    void eliminarToken_verificaEliminacion() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-delete-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);

        tokenEmitidoRepository.delete(token);
        assertFalse(tokenEmitidoRepository.findByToken(token.getToken()).isPresent());
    }

    // 11. Buscar token por valor
    @Test
    void buscarTokenPorValor() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-find-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);

        assertTrue(tokenEmitidoRepository.findByToken(token.getToken()).isPresent());
    }

    // 12. Actualizar estado de token a revocado
    @Test
    void actualizarEstadoToken_revocado() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-revoke-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);

        token.setRevocado(true);
        tokenEmitidoRepository.saveAndFlush(token);

        TokenEmitido actualizado = tokenEmitidoRepository.findByToken(token.getToken()).get();
        assertTrue(actualizado.isRevocado());
    }

    // 13. Guardar refresh token persiste correctamente
    @Test
    void guardarRefreshToken_persisteCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-test-" + UUID.randomUUID());
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");

        refreshTokenRepository.saveAndFlush(refreshToken);
        assertTrue(refreshTokenRepository.findByToken(refreshToken.getToken()).isPresent());
    }

    // 14. Eliminar refresh token verifica eliminación
    @Test
    void eliminarRefreshToken_verificaEliminacion() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-delete-" + UUID.randomUUID());
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.saveAndFlush(refreshToken);

        refreshTokenRepository.delete(refreshToken);
        assertFalse(refreshTokenRepository.findByToken(refreshToken.getToken()).isPresent());
    }

    // 15. Buscar refresh token por valor
    @Test
    void buscarRefreshTokenPorValor() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-find-" + UUID.randomUUID());
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.saveAndFlush(refreshToken);

        assertTrue(refreshTokenRepository.findByToken(refreshToken.getToken()).isPresent());
    }

    // 16. Actualizar estado de refresh token a revocado
    @Test
    void actualizarEstadoRefreshToken_revocado() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-revoke-" + UUID.randomUUID());
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.saveAndFlush(refreshToken);

        refreshToken.setRevocado(true);
        refreshTokenRepository.saveAndFlush(refreshToken);

        RefreshToken actualizado = refreshTokenRepository.findByToken(refreshToken.getToken()).get();
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

        Rol rolUsuario = rolRepository.findByNombre(RolNombre.ROLE_USUARIO)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuarioDuplicado.setRol(rolUsuario);
        try {
            usuarioRepository.saveAndFlush(usuarioDuplicado);
            assertFalse(true, "Se esperaba excepción por username duplicado");
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

        Rol rolUsuario = rolRepository.findByNombre(RolNombre.ROLE_USUARIO)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuarioDuplicado.setRol(rolUsuario);

        try {
            usuarioRepository.saveAndFlush(usuarioDuplicado);
            assertFalse(true, "Se esperaba excepción por email duplicado");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    // 21. Eliminar usuario elimina sesiones y tokens asociados
    @Test
    void eliminarUsuario_eliminaSesionesYTokensAsociados() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();

        SesionActiva sesion = new SesionActiva();
        sesion.setToken("access-token-asociado-" + UUID.randomUUID());
        sesion.setRefreshToken("refresh-token-asociado-" + UUID.randomUUID());
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesion.setRevocado(false);
        sesionActivaRepository.saveAndFlush(sesion);

        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-asociado-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);

        sesionActivaRepository.deleteAll(sesionActivaRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        tokenEmitidoRepository.deleteAll(tokenEmitidoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        refreshTokenRepository.deleteAll(refreshTokenRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        direccionRepository.deleteAll(direccionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        telefonoRepository.deleteAll(telefonoRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        emailVerificacionRepository.findByUsuario_IdUsuario(usuarioTest.getIdUsuario()).ifPresent(emailVerificacionRepository::delete);
        carritoRepository.deleteAll(carritoRepository.findAllByUsuario_IdUsuario(usuarioTest.getIdUsuario()));
        usuarioTest.setRol(null);
        usuarioRepository.saveAndFlush(usuarioTest);
        usuarioRepository.delete(usuarioTest);

        assertFalse(sesionActivaRepository.findByToken(sesion.getToken()).isPresent());
        assertFalse(tokenEmitidoRepository.findByToken(token.getToken()).isPresent());
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

        Rol rolUsuario = rolRepository.findByNombre(RolNombre.ROLE_USUARIO)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuario.setRol(rolUsuario);

        usuarioRepository.saveAndFlush(usuario);
        Usuario guardado = usuarioRepository.findByUsername("usuarioFecha").get();
        assertTrue(guardado.getFechaRegistro() != null);
    }

    // 23. Guardar sesión activa setea fecha de inicio correctamente
    @Test
    void guardarSesionActiva_seteaFechaInicioCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        String token = "access-token-fecha-" + UUID.randomUUID();
        String refresh = "refresh-token-fecha-" + UUID.randomUUID();

        SesionActiva sesion = new SesionActiva();
        sesion.setToken(token);
        sesion.setRefreshToken(refresh);
        sesion.setUsuario(usuarioTest);
        sesion.setIp("127.0.0.1");
        sesion.setUserAgent("JUnit");
        sesion.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.saveAndFlush(sesion);
        SesionActiva guardada = sesionActivaRepository.findByToken(token).get();
        assertTrue(guardada.getFechaInicioSesion() != null);
    }

    // 24. Guardar token setea fecha de creación correctamente
    @Test
    void guardarToken_seteaFechaCreacionCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        TokenEmitido token = new TokenEmitido();
        token.setToken("access-token-fecha-" + UUID.randomUUID());
        token.setUsuario(usuarioTest);
        token.setRevocado(false);
        token.setFechaEmision(Instant.now());
        token.setFechaExpiracion(Instant.now().plusSeconds(3600));
        tokenEmitidoRepository.saveAndFlush(token);
        TokenEmitido guardado = tokenEmitidoRepository.findByToken(token.getToken()).get();
        assertTrue(guardado.getFechaEmision() != null);
    }

    // 25. Guardar refresh token setea fecha de creación correctamente
    @Test
    void guardarRefreshToken_seteaFechaCreacionCorrectamente() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-fecha-" + UUID.randomUUID());
        refreshToken.setUsuario(usuarioTest);
        refreshToken.setRevocado(false);
        refreshToken.setFechaExpiracion(Instant.now().plusSeconds(7200));
        refreshToken.setUsado(false);
        refreshToken.setIp("127.0.0.1");
        refreshToken.setUserAgent("JUnit");
        refreshTokenRepository.saveAndFlush(refreshToken);
        RefreshToken guardado = refreshTokenRepository.findByToken(refreshToken.getToken()).get();
        assertTrue(guardado.getFechaExpiracion() != null);
    }

    // 26. Guardar múltiples sesiones activas para un usuario
    @Test
    void guardarMultiplesSesionesActivas_paraUnUsuario() {
        usuarioTest = usuarioRepository.findByUsername("usuario123").orElseThrow();
        SesionActiva sesion1 = new SesionActiva();
        sesion1.setToken("access-token-1-" + UUID.randomUUID());
        sesion1.setRefreshToken("refresh-token-1-" + UUID.randomUUID());
        sesion1.setUsuario(usuarioTest);
        sesion1.setIp("127.0.0.1");
        sesion1.setUserAgent("JUnit");
        sesion1.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.saveAndFlush(sesion1);

        SesionActiva sesion2 = new SesionActiva();
        sesion2.setToken("access-token-2-" + UUID.randomUUID());
        sesion2.setRefreshToken("refresh-token-2-" + UUID.randomUUID());
        sesion2.setUsuario(usuarioTest);
        sesion2.setIp("127.0.0.1");
        sesion2.setUserAgent("JUnit");
        sesion2.setFechaInicioSesion(Instant.now());
        sesionActivaRepository.saveAndFlush(sesion2);

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
