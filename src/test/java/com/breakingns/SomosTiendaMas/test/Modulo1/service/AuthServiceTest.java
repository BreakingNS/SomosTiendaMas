package com.breakingns.SomosTiendaMas.test.Modulo1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.LoginAttempt;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILoginAttemptRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import com.breakingns.SomosTiendaMas.security.exception.TokenRevocadoException;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

/*                                                  AuthServiceTest
    
    Lógica de autenticación

        1. Permite login con credenciales correctas
        2. Rechaza login con credenciales incorrectas
        3. Rechaza login de usuario desactivado

    Generación de tokens

        4. Genera access y refresh tokens al hacer login
        5. Los tokens generados tienen formato y expiración correctos

    Validación y revocación de tokens

        6. Valida correctamente un access token válido
        7. Valida correctamente un refresh token válido
        8. Revoca el access token y no permite su uso posterior
        9. Revoca el refresh token y no permite su uso posterior

    Manejo de sesiones activas

        10. Crea una sesión activa al hacer login
        11. Revoca la sesión activa al hacer logout
        12. Permite listar las sesiones activas de un usuario
        13. Permite cerrar todas las sesiones activas excepto la actual

    Validación de errores

        14. Retorna 401 si el token está expirado
        15. Lanza excepción si el usuario no existe
        16. Lanza excepción si el token está revocado
        17. Lanza excepción si el refresh token es inválido

    Intentos fallidos y bloqueo de cuenta

        18. Incrementa el contador de intentos fallidos al login incorrecto
        19. Bloquea la cuenta si se superan los intentos máximos
        20. Rechaza login si la cuenta está bloqueada

    Verificación de email en el login

        21. Rechaza login si el email no está verificado
        22. Permite login si el email está verificado

    Login de usuario desactivado

        23. Rechaza login si el usuario está desactivado

    Auditoría o logging de eventos de autenticación

        24. Genera evento de auditoría en login exitoso
        25. Genera evento de auditoría en login fallido

    Pruebas de concurrencia (race conditions)

        26. Mantiene la integridad de sesiones en login concurrente
        27. No duplica tokens en refresh concurrente

    Pruebas de roles y permisos en el service

        28. Permite login con rol admin
        29. Permite login con rol usuario

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

    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final ILoginAttemptRepository loginAttemptRepository;
    private final IRolRepository rolRepository;
    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;

    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;

    @MockBean
    private EmailService emailService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;
    
    @Autowired
    private ITokenEmitidoRepository tokenEmitidoRepository;
    @Autowired
    private IRefreshTokenRepository refreshTokenRepository;

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

    // Lógica de autenticación

    // 1. Permite login con credenciales correctas
    @Test
    void loginConCredencialesCorrectas_exitoso() throws Exception {
        // El usuario ya está registrado en el @BeforeEach

        // Login por endpoint usando los datos del usuario base
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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
    
    // 2. Rechaza login con credenciales incorrectas
    @Test
    void loginConCredencialesIncorrectas_falla() throws Exception {
        // El usuario ya está registrado en el @BeforeEach

        // Intentar login con contraseña incorrecta
        MvcResult loginResult = loginUsuario("usuario123", "ClaveIncorrecta");
        assertEquals(401, loginResult.getResponse().getStatus());
    }
    
    // Generación de tokens
     
    // 4. Genera access y refresh tokens al hacer login
    @Test
    void generarTokensAlLogin_exitoso() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertFalse(sesiones.isEmpty());
        assertEquals(accessToken, sesiones.get(0).getToken());
        assertEquals(refreshToken, sesiones.get(0).getRefreshToken());
    }

    // 5. Los tokens generados tienen formato y expiración correctos
    @Test
    void formatoYExpiracionTokens_generadosCorrectamente() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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
    
    // Validación y revocación de tokens
    
    // 6. Valida correctamente un access token válido
    @Test
    void validarAccessToken_valido() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken);

        // Validar el access token usando el servicio real
        boolean esValido = authService.validarAccessToken(accessToken);
        assertTrue(esValido, "El access token debería ser válido");
    }

    // 7. Valida correctamente un refresh token válido
    @Test
    void validarRefreshToken_valido() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(refreshToken);

        // Validar el refresh token usando el servicio real
        boolean esValido = authService.validarRefreshToken(refreshToken);
        assertTrue(esValido, "El refresh token debería ser válido");
    }

    // 8. Revoca el access token y no permite su uso posterior
    @Test
    void revocarAccessToken_noSePuedeUsar() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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

        // Verificar que no se puede usar el token (debe lanzar excepción)
        assertThrows(TokenRevocadoException.class, () -> authService.validarAccessToken(accessToken));
    }   
    
    // 9. Revoca el refresh token y no permite su uso posterior
    @Test
    void revocarRefreshToken_noSePuedeUsar() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener refresh token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
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

    // Manejo de sesiones activas

    // 10. Crea una sesión activa al hacer login
    @Test
    void crearSesionActivaAlLogin() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Verificar que el usuario existe en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Verificar que la sesión activa existe en la BD y no está revocada
        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertFalse(sesiones.isEmpty(), "Debe existir al menos una sesión activa para el usuario");
        SesionActiva sesion = sesiones.get(0);
        assertEquals(usuario.getIdUsuario(), sesion.getUsuario().getIdUsuario());
        assertFalse(sesion.isRevocado(), "La sesión activa no debe estar revocada");
    }

    // 11. Revoca la sesión activa al hacer logout
    @Test
    void revocarSesionActiva_logout() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken);

        // Ejecutar logout real
        authService.logout(accessToken);

        // Verificar que la sesión activa está revocada en la BD
        Optional<SesionActiva> sesionOpt = sesionActivaRepository.findByToken(accessToken);
        assertTrue(sesionOpt.isPresent());
        assertTrue(sesionOpt.get().isRevocado(), "La sesión activa debe estar revocada después del logout");
    }
    
    // 12. Permite listar las sesiones activas de un usuario
    @Test
    void listarSesionesActivasUsuario() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Obtener access token de las cookies
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken);

        // Llamar al endpoint de sesiones activas
        MvcResult sesionesResult = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificar que la respuesta contiene al menos una sesión activa
        String json = sesionesResult.getResponse().getContentAsString();
        SesionActivaDTO[] sesiones = objectMapper.readValue(json, SesionActivaDTO[].class);
        assertTrue(sesiones.length > 0, "Debe existir al menos una sesión activa para el usuario");
        assertTrue(Arrays.stream(sesiones).allMatch(s -> !s.revocado()), "Las sesiones no deben estar revocadas");
    }

    // 13. Permite cerrar todas las sesiones activas excepto la actual
    @Test
    void cerrarOtrasSesionesActivas_endpoint() throws Exception {
        // Login por endpoint (primera sesión)
        MvcResult loginResult1 = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult1.getResponse().getStatus());
        String accessToken1 = Arrays.stream(loginResult1.getResponse().getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken1);

        // Login por endpoint (segunda sesión)
        MvcResult loginResult2 = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult2.getResponse().getStatus());
        String accessToken2 = Arrays.stream(loginResult2.getResponse().getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(accessToken2);

        // Llamar al endpoint para cerrar otras sesiones (usando la segunda sesión como actual)
        mockMvc.perform(post("/api/sesiones/private/logout-otras-sesiones")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificar que solo queda la sesión actual activa
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertEquals(1, sesiones.size(), "Solo debe quedar una sesión activa");
        assertEquals(accessToken2, sesiones.get(0).getToken(), "La sesión activa debe ser la actual");
    }

    // Validación de errores

    // 14. Retorna 401 si el token está expirado
    @Test
    void tokenExpiradoDebeRetornarUnauthorized() throws Exception {
        // Login y obtención de tokens
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");

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

        loginResult = mockMvc.perform(post("/test/api/auth/public/refresh-token")
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

        // Esperar a que el access token expire (ajusta el tiempo según tu configuración)
        Thread.sleep(3100);

        // Intentar acceder a un endpoint protegido con el token expirado
        MvcResult result = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus(), "El access token expirado debe retornar 401 Unauthorized");

        // Esperar a que el refresh token también expire (ajusta el tiempo según tu configuración)
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
    
    // 15. Lanza excepción si el usuario no existe
    @Test
    void usuarioNoEncontrado_lanzaExcepcion() throws Exception {
        // Intentar login con usuario que no existe
        LoginRequest loginRequest = new LoginRequest("usuarioNoExist", "ContraIncorre");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "MockMvc"))
                .andReturn();

        // Verificar que la respuesta sea 401 Unauthorized
        assertEquals(401, loginResult.getResponse().getStatus());
    }

    // 16. Lanza excepción si el token está revocado
    @Test
    void tokenRevocado_lanzaExcepcion_integracion() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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

    // 17. Lanza excepción si el refresh token es inválido
    @Test
    void refreshTokenInvalido_lanzaExcepcion_integracion() throws Exception {
        // Login por endpoint usando el usuario registrado en @BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
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
    
    // Intentos fallidos y bloqueo de cuenta

    // 18. Incrementa el contador de intentos fallidos al login incorrecto
    @Test
    void loginIntentosFallidos_incrementaContador() throws Exception {
        // Intentar login con contraseña incorrecta varias veces
        for (int i = 0; i < 2; i++) {
            MvcResult loginResult = loginUsuario("usuario123", "ClaveIncorrecta");
            assertEquals(401, loginResult.getResponse().getStatus());
        }

        // Verificar que el contador de intentos fallidos se incrementó
        // (ajusta según tu implementación, por ejemplo consultando la tabla login_failed_attempts)
        int intentos = loginAttemptService.traerIntentosFallidos("usuario123", "127.0.0.1"); // Ajusta la IP según sea necesario

        System.out.println("\n\nIntentos fallidos registrados: " + intentos);

        assertTrue(intentos >= 2, "El contador de intentos fallidos debe incrementarse");
    }
    
    // 19. Bloquea la cuenta si se superan los intentos máximos
    @Test
    void cuandoSuperaIntentosFallidos_usuarioQuedaBloqueadoYSeAsignaTiempo() {
        String username = "usuario123";
        String ip = "127.0.0.1";

        // Simula 5 intentos fallidos de login
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(username, ip);
        }

        // Verifica que el usuario está bloqueado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        assertTrue(usuario.isCuentaBloqueada(), "El usuario debería estar bloqueado después de 5 intentos fallidos");

        // Verifica que el registro en login_failed_attempts tiene blocked_until asignado
        Integer intentos = loginAttemptService.traerIntentosFallidos(username, ip);
        assertTrue(intentos >= 5, "El contador de intentos fallidos debe ser al menos 5");

        // Consulta el registro en la tabla
        List<LoginAttempt> attempts = loginAttemptRepository.findAll();
        LoginAttempt attempt = attempts.stream()
            .filter(a -> username.equals(a.getUsername()) && ip.equals(a.getIp()))
            .findFirst()
            .orElse(null);

        assertNotNull(attempt, "Debe existir el registro de intentos fallidos");
        assertNotNull(attempt.getBlockedUntil(), "El campo blocked_until debe estar asignado");
    }
    
    // 20. Rechaza login si la cuenta está bloqueada
    @Test
    void loginConCuentaBloqueada_falla() throws Exception {
        String username = "usuario123";
        String ip = "127.0.0.1";

        // Simula 5 intentos fallidos para bloquear la cuenta
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(username, ip);
        }

        // Verifica que la cuenta está bloqueada
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        assertTrue(usuario.isCuentaBloqueada(), "La cuenta debe estar bloqueada");

        // Intenta login con la cuenta bloqueada
        MvcResult loginResult = loginUsuario(username, "ClaveSegura123");
        assertEquals(429, loginResult.getResponse().getStatus(), "El login debe ser rechazado para una cuenta bloqueada");
    }

    // Verificación de email en el login
    
    // 21. Rechaza login si el email no está verificado
    @Test
    void loginConEmailVerificado_falla() throws Exception {
        // Asegura que el email está NO verificado en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(false);
        usuarioRepository.save(usuario);

        // Intentar login
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(401, loginResult.getResponse().getStatus(), "El login debe ser rechazado si el email no está verificado");
    }
    
    // 22. Permite login si el email está verificado
    @Test
    void loginConEmailVerificado_exitoso() throws Exception {
        // Intentar login
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus(), "El login debe ser exitoso si el email está verificado");
    }
    
    // Login de usuario desactivado

    // 23. Rechaza login si el usuario está desactivado
    @Test
    void loginConUsuarioDesactivado_falla() throws Exception {
        // Registrar usuario normalmente (ya se hace en @BeforeEach)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Desactivar el usuario
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // Intentar login con el usuario desactivado
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");

        // Esperar que falle con 401 o excepción específica
        assertEquals(401, loginResult.getResponse().getStatus());
    }
    
    // Auditoría o logging de eventos de autenticación

    // 24. Genera evento de auditoría en login exitoso
    @Test
    void loginGeneraEventoAuditoria_exitoso() throws Exception {

        // Login exitoso
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        // Verificar que se generó el evento de auditoría (ajusta según tu implementación)
        boolean eventoGenerado = authService.seGeneroEventoAuditoria("usuario123", "LOGIN_EXITOSO");
        assertTrue(eventoGenerado, "Debe generarse evento de auditoría en login exitoso");
    }
    
    // 25. Genera evento de auditoría en login fallido
    @Test
    void loginFallaGeneraEventoAuditoria() throws Exception {

        // Login fallido
        MvcResult loginResult = loginUsuario("usuario123", "ClaveIncorrecta");
        assertEquals(401, loginResult.getResponse().getStatus());

        // Verificar que se generó el evento de auditoría
        boolean eventoGenerado = authService.seGeneroEventoAuditoria("usuario123", "LOGIN_FALLIDO");
        assertTrue(eventoGenerado, "Debe generarse evento de auditoría en login fallido");

        // Verificar que se guardó el intento fallido en login_failed_attempts
        List<LoginAttempt> attempts = loginAttemptRepository.findAll();
        LoginAttempt attempt = attempts.stream()
            .filter(a -> "usuario123".equals(a.getUsername()))
            .findFirst()
            .orElse(null);

        assertNotNull(attempt, "Debe existir el registro de intento fallido en login_failed_attempts");
        assertTrue(attempt.getFailedAttempts() >= 1, "El contador de intentos fallidos debe ser al menos 1");
    }
    
    // Pruebas de concurrencia (race conditions)
    
    // 26. Mantiene la integridad de sesiones en login concurrente
    @Test
    void loginConcurrente_mantieneIntegridadSesiones() throws Exception {
        // 1. Verificar y asegurar que el usuario tiene el email verificado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent(), "El usuario debe existir en la base de datos");
        Usuario usuario = usuarioOpt.get();
        
        // 2. Simular dos logins concurrentes para el mismo usuario
        MvcResult loginResult1 = loginUsuario("usuario123", "ClaveSegura123");
        MvcResult loginResult2 = loginUsuario("usuario123", "ClaveSegura123");

        assertEquals(200, loginResult1.getResponse().getStatus(), "El primer login debe ser exitoso");
        assertEquals(200, loginResult2.getResponse().getStatus(), "El segundo login debe ser exitoso");

        // 3. Verificar que ambas sesiones existen y no están revocadas
        usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent(), "El usuario debe existir en la base de datos");
        usuario = usuarioOpt.get();

        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuario.getIdUsuario());
        assertEquals(2, sesiones.size(), "Debe haber dos sesiones activas concurrentes para el usuario");
        assertTrue(sesiones.stream().allMatch(s -> !s.isRevocado()), "Ninguna sesión debe estar revocada");
    }
    
    // 27. No duplica tokens en refresh concurrente
    @Test
    void refreshTokenConcurrente_noDuplicaTokens() throws Exception {
        
        // 2. Login para obtener el refresh token
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        assertNotNull(refreshToken, "El refresh token debe existir en las cookies");

        // 3. Simular dos requests de refresh concurrentes con el mismo token
        MvcResult refreshResult1 = mockMvc.perform(post("/api/auth/public/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                .header("User-Agent", "MockMvc"))
                .andReturn();

        MvcResult refreshResult2 = mockMvc.perform(post("/api/auth/public/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                .header("User-Agent", "MockMvc"))
                .andReturn();

        assertEquals(200, refreshResult1.getResponse().getStatus(), "El primer refresh debe ser exitoso");
        assertEquals(401, refreshResult2.getResponse().getStatus(), "El segundo refresh debe fallar por token revocado");

        // 4. Verificar que no se duplicaron los tokens en la base de datos
        List<RefreshToken> tokens = refreshTokenRepository.findAllByToken(refreshToken);
        assertEquals(1, tokens.size(), "No debe haber duplicados de refresh token en la base de datos");
    }
    
    // Pruebas de roles y permisos en el service

    // 28. Permite login con rol admin
    @Test
    void loginConRolAdmin_exitoso() throws Exception {
        // Verificar y asegurar que el usuario tiene el email verificado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent(), "El usuario debe existir en la base de datos");
        Usuario usuario = usuarioOpt.get();

        // Buscar el rol admin en la base de datos y asignarlo
        Rol rolAdmin = rolRepository.findByNombre(RolNombre.ROLE_ADMIN)
            .orElseThrow(() -> new RuntimeException("Rol admin no existe"));
        usuario.setRol(rolAdmin);
        usuarioRepository.save(usuario);

        // Intentar login con usuario admin
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus(), "El login debe ser exitoso para usuario con rol admin y email verificado");
    }
    
    // 29. Permite login con rol usuario
    @Test
    void loginConRolUsuario_exitoso() throws Exception {
        // El usuario ya tiene rol usuario por defecto en el BeforeEach
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        assertEquals(200, loginResult.getResponse().getStatus());
    }
    
}
