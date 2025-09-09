package com.breakingns.SomosTiendaMas.test.Modulo1.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import com.breakingns.SomosTiendaMas.auth.model.LoginAttempt;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IEmailVerificacionRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILoginAttemptRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenResetPasswordRepository;
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
import org.mockito.ArgumentCaptor;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

/*                                      EmailVerificationTest

    Verificación de email al registrarse
        1. Envía email de verificación al usuario nuevo
        2. Genera y guarda código de verificación correctamente
        3. No permite verificar email con código inválido
        4. No permite verificar email con código expirado
        5. No permite verificar email con código ya usado
        6. Permite verificar email con código válido y actualiza estado del usuario
        7. No permite login si el email no está verificado

    Recuperación de contraseña
        8. Envía email de recuperación de contraseña al usuario
        9. Genera y guarda código de recuperación correctamente
        10. No permite recuperar contraseña con código inválido
        11. No permite recuperar contraseña con código expirado
        12. No permite recuperar contraseña con código ya usado
        13. Permite recuperar contraseña con código válido y actualiza la contraseña

    Casos generales y de seguridad
        14. No envía email si el usuario no existe
        15. No expone el código de verificación en la respuesta
        16. El código de verificación es único y aleatorio
        17. El email enviado tiene el formato y contenido esperado

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
            "DELETE FROM usuario_roles",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )/*,
    @Sql(
        statements = {
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
            "DELETE FROM usuario_roles",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )*/
})
class EmailVerificationTest {

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

    private final LoginAttemptService loginAttemptService;

    private final IUsuarioRepository usuarioRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final ILoginAttemptRepository loginAttemptRepository;
    private final IRolRepository rolRepository;
    private final IEmailVerificacionRepository emailVerificacionRepository;
    private final ITokenResetPasswordRepository tokenResetPasswordRepository;

    private final AuthService authService;

    private final PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    private RegistroUsuarioCompletoDTO registroDTO;

    @BeforeEach
    void setUp() throws Exception {
        // Crear usuario base para los tests
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

        // Registrar usuario antes de cada test
        registrarUsuarioCompleto(registroDTO);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
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

    @Test
	void testBasico() {
		assertTrue(true);
	}

    // Verificación de email al registrarse

    // 1. Envía email de verificación al usuario nuevo
    @Test
    void enviarEmailVerificacion_usuarioNuevo() throws Exception {
        // Crear usuario base para los tests
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuario123");
        usuarioDTO.setEmail("correoprueba1@noenviar.com");
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

        // Registrar usuario antes de cada test
        registrarUsuarioCompleto(registroDTO);

        // Verificar que se creó el registro de verificación de email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        Optional<EmailVerificacion> tokenOpt = emailVerificacionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(tokenOpt.isPresent(), "Debe existir un token de verificación para el usuario");
    }
    
    // 2. Genera y guarda código de verificación correctamente
    @Test
    void generaYGuardaCodigoVerificacion_correctamente() throws Exception {
        // Registrar usuario de prueba
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioTest2");
        usuarioDTO.setEmail("correoprueba1@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Ana");
        usuarioDTO.setApellidoResponsable("García");
        usuarioDTO.setDocumentoResponsable("87654321");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1995, 5, 5));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroDireccionDTO direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Nueva");
        direccionDTO.setNumero("456");
        direccionDTO.setCiudad("Ciudad");
        direccionDTO.setProvincia("Provincia");
        direccionDTO.setCodigoPostal("2000");
        direccionDTO.setPais("Argentina");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("2233445566");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        registrarUsuarioCompleto(registroDTO);

        // Buscar el usuario recién creado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuarioTest2");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Buscar el token de verificación en la BD
        Optional<EmailVerificacion> tokenOpt = emailVerificacionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(tokenOpt.isPresent(), "Debe existir un token de verificación para el usuario");

        EmailVerificacion token = tokenOpt.get();
        assertNotNull(token.getCodigo(), "El código de verificación debe existir");
        assertFalse(token.isUsado(), "El código de verificación no debe estar usado");
        assertTrue(token.getFechaExpiracion().isAfter(token.getFechaCreacion()), "La expiración debe ser posterior a la creación");
        assertEquals(usuario.getIdUsuario(), token.getUsuario().getIdUsuario(), "El token debe estar asociado al usuario correcto");
    }
    
    // 3. No permite verificar email con código inválido
    @Test
    void noPermiteVerificarEmail_codigoInvalido() throws Exception {
        String codigoInvalido = "codigo-que-no-existe";

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/verificar-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"" + codigoInvalido + "\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 404, "Debe responder con error si el código es inválido");
    }
    
    // 4. No permite verificar email con código expirado
    @Test
    void noPermiteVerificarEmail_codigoExpirado() throws Exception {
        // Crear usuario y token expirado manualmente
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        EmailVerificacion tokenExpirado = new EmailVerificacion(
            "codigo-expirado",
            usuario,
            LocalDateTime.now().minusMinutes(1) // Expirado
        );
        tokenExpirado.setFechaCreacion(LocalDateTime.now().minusHours(1));
        emailVerificacionRepository.save(tokenExpirado);

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/verificar-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"codigo-expirado\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 410, "Debe responder con error si el código está expirado");
    }

    // 5. No permite verificar email con código ya usado
    @Test
    void noPermiteVerificarEmail_codigoYaUsado() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        EmailVerificacion tokenUsado = new EmailVerificacion(
            "codigo-usado",
            usuario,
            LocalDateTime.now().plusMinutes(10)
        );
        tokenUsado.setUsado(true);
        tokenUsado.setFechaCreacion(LocalDateTime.now().minusMinutes(5));
        emailVerificacionRepository.save(tokenUsado);

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/verificar-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"codigo-usado\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 409, "Debe responder con error si el código ya fue usado");
    }

    // 6. Permite verificar email con código válido y actualiza estado del usuario
    @Test
    void permiteVerificarEmail_codigoValido_actualizaEstadoUsuario() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        EmailVerificacion tokenValido = new EmailVerificacion(
            "codigo-valido",
            usuario,
            LocalDateTime.now().plusMinutes(10)
        );
        tokenValido.setFechaCreacion(LocalDateTime.now());
        emailVerificacionRepository.save(tokenValido);

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/verificar-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"codigo-valido\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(200, status, "Debe responder OK si el código es válido");

        // Verifica que el usuario ahora tiene el email verificado
        Usuario usuarioVerificado = usuarioRepository.findByUsername("usuario123").get();
        assertTrue(usuarioVerificado.getEmailVerificado(), "El usuario debe tener el email verificado");
    }

    // 7. No permite login si el email no está verificado
    @Test
    void noPermiteLogin_emailNoVerificado() throws Exception {
        // Buscar usuario y marcarlo como no verificado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(false);
        usuarioRepository.save(usuario);

        // Intentar login
        MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
        int status = loginResult.getResponse().getStatus();
        assertEquals(401, status, "No debe permitir login si el email no está verificado");
    }

    // Recuperación de contraseña

    // 8. Envía email de recuperación de contraseña al usuario
    @Test
    void enviarEmailRecuperacion_usuario() throws Exception {
        // Simular solicitud de recuperación de contraseña
        String email = "correoprueba@noenviar.com";
        MvcResult result = mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(200, status, "Debe responder OK al solicitar recuperación de contraseña");

        // Buscar el usuario por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Verificar que se creó el registro de recuperación en la tabla tokens_reset_password
        Optional<TokenResetPassword> tokenOpt = tokenResetPasswordRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(tokenOpt.isPresent(), "Debe existir un token de recuperación para el usuario");
    }

    // 9. Genera y guarda código de recuperación correctamente
    @Test
    void generaYGuardaCodigoRecuperacion_correctamente() throws Exception {
        // Simular solicitud de recuperación de contraseña
        String email = "correoprueba@noenviar.com";
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andReturn();

        // Buscar el usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Buscar el token de recuperación en la tabla tokens_reset_password
        Optional<TokenResetPassword> tokenOpt = tokenResetPasswordRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(tokenOpt.isPresent(), "Debe existir un token de recuperación para el usuario");

        TokenResetPassword token = tokenOpt.get();
        assertNotNull(token.getToken(), "El código de recuperación debe existir");
        assertFalse(token.isUsado(), "El código de recuperación no debe estar usado");
        assertTrue(token.getFechaExpiracion().isAfter(token.getFechaCreacion()), "La expiración debe ser posterior a la creación");
        assertEquals(usuario.getIdUsuario(), token.getUsuario().getIdUsuario(), "El token debe estar asociado al usuario correcto");
    }
     
    /// 10. No permite recuperar contraseña con token inválido
    @Test
    void noPermiteRecuperarContrasena_tokenInvalido() throws Exception {
        String tokenInvalido = "token-que-no-existe";

        MvcResult result = mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenInvalido + "\", \"nuevaPassword\":\"NuevaClave123\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 404, "Debe responder con error si el token es inválido");
    }

    // 11. No permite recuperar contraseña con token expirado
    @Test
    void noPermiteRecuperarContrasena_tokenExpirado() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail("correoprueba@noenviar.com");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Crear token de 32 caracteres ya expirado
        String tokenExpirado = TokenResetPassword.generarTokenAlfanumerico(32);
        Instant fechaExpiracion = Instant.now().minusSeconds(60); // Expirado hace 1 minuto
        TokenResetPassword token = new TokenResetPassword(tokenExpirado, usuario, fechaExpiracion);
        tokenResetPasswordRepository.save(token);

        // Intentar recuperar contraseña con el token expirado
        MvcResult result = mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenExpirado + "\", \"nuevaPassword\":\"NuevaClave123\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 410, "Debe responder con error si el token está expirado");
    }

    // 12. No permite recuperar contraseña con token ya usado
    @Test
    void noPermiteRecuperarContrasena_tokenYaUsado() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Crear token de 32 caracteres válido pero ya usado
        String tokenUsado = TokenResetPassword.generarTokenAlfanumerico(32);
        Instant fechaExpiracion = Instant.now().plusSeconds(3600); // Expira en 1 hora
        TokenResetPassword token = new TokenResetPassword(tokenUsado, usuario, fechaExpiracion);
        token.setUsado(true); // Marcar como usado
        tokenResetPasswordRepository.save(token);

        // Intentar recuperar contraseña con el token ya usado
        MvcResult result = mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenUsado + "\", \"nuevaPassword\":\"NuevaClave123\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 409, "Debe responder con error si el token ya fue usado");
    }
    
    // 13. Permite recuperar contraseña con token válido y actualiza la contraseña
    @Test
    void permiteRecuperarContrasena_tokenValido_actualizaContrasena() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        // Crear token válido (no expirado, no usado)
        String tokenValido = TokenResetPassword.generarTokenAlfanumerico(32);
        Instant fechaExpiracion = Instant.now().plusSeconds(3600); // Expira en 1 hora
        TokenResetPassword token = new TokenResetPassword(tokenValido, usuario, fechaExpiracion);
        token.setUsado(false);
        tokenResetPasswordRepository.save(token);

        String nuevaPassword = "NuevaClave123";
        MvcResult result = mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + tokenValido + "\", \"nuevaPassword\":\"" + nuevaPassword + "\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(200, status, "Debe responder OK si el token es válido");

        // Verifica que la contraseña del usuario fue actualizada (usando el encoder)
        Usuario usuarioActualizado = usuarioRepository.findByUsername("usuario123").get();
        assertTrue(passwordEncoder.matches(nuevaPassword, usuarioActualizado.getPassword()),
                "La contraseña debe haber sido actualizada");
    }

    // Casos generales y de seguridad

    // 14. No envía email si el usuario no existe
    @Test
    void noEnviaEmail_usuarioNoExiste() throws Exception {
        String emailInexistente = "usuarionoexiste@noenviar.com";
        MvcResult result = mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + emailInexistente + "\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(200, status, "Debe responder OK aunque el usuario no exista (por seguridad)");

        // Verifica que no se creó ningún registro de recuperación
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(emailInexistente);
        assertTrue(usuarioOpt.isEmpty(), "No debe existir el usuario en la base");

        Optional<TokenResetPassword> tokenOpt = tokenResetPasswordRepository.findByUsuario_IdUsuario(-1L);
        assertTrue(tokenOpt.isEmpty(), "No debe existir un token de recuperación para el usuario inexistente");
    }

    // 15. No expone el código de verificación en la respuesta
    @Test
    void noExponeCodigoVerificacion_enRespuesta() throws Exception {
        String email = "correoprueba1@noenviar.com";
        MvcResult result = mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertFalse(responseBody.contains("codigo"), "La respuesta no debe exponer el código de verificación");
        assertFalse(responseBody.contains("token"), "La respuesta no debe exponer el token de recuperación");
    }
    
    // 16. El código de verificación es único y aleatorio
    @Test
    void codigoVerificacion_esUnicoYAleatorio() throws Exception {
        // Registrar usuario A
        RegistroUsuarioDTO usuarioDTOA = new RegistroUsuarioDTO();
        usuarioDTOA.setUsername("usuarioA");
        usuarioDTOA.setEmail("correoprueba1@noenviar.com");
        usuarioDTOA.setPassword("ClaveSegura123");
        usuarioDTOA.setNombreResponsable("A");
        usuarioDTOA.setApellidoResponsable("A");
        usuarioDTOA.setDocumentoResponsable("11111111");
        usuarioDTOA.setTipoUsuario("PERSONA_FISICA");
        usuarioDTOA.setAceptaTerminos(true);
        usuarioDTOA.setAceptaPoliticaPriv(true);
        usuarioDTOA.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTOA.setGeneroResponsable("MASCULINO");
        usuarioDTOA.setIdioma("es");
        usuarioDTOA.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTOA.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO registroDTOA = new RegistroUsuarioCompletoDTO();
        registroDTOA.setUsuario(usuarioDTOA);
        registroDTOA.setDirecciones(List.of());
        registroDTOA.setTelefonos(List.of());

        registrarUsuarioCompleto(registroDTOA);

        // Registrar usuario B
        RegistroUsuarioDTO usuarioDTOB = new RegistroUsuarioDTO();
        usuarioDTOB.setUsername("usuarioB");
        usuarioDTOB.setEmail("correoprueba2@noenviar.com");
        usuarioDTOB.setPassword("ClaveSegura123");
        usuarioDTOB.setNombreResponsable("B");
        usuarioDTOB.setApellidoResponsable("B");
        usuarioDTOB.setDocumentoResponsable("22222222");
        usuarioDTOB.setTipoUsuario("PERSONA_FISICA");
        usuarioDTOB.setAceptaTerminos(true);
        usuarioDTOB.setAceptaPoliticaPriv(true);
        usuarioDTOB.setFechaNacimientoResponsable(LocalDate.of(1991, 2, 2));
        usuarioDTOB.setGeneroResponsable("FEMENINO");
        usuarioDTOB.setIdioma("es");
        usuarioDTOB.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTOB.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO registroDTOB = new RegistroUsuarioCompletoDTO();
        registroDTOB.setUsuario(usuarioDTOB);
        registroDTOB.setDirecciones(List.of());
        registroDTOB.setTelefonos(List.of());

        registrarUsuarioCompleto(registroDTOB);

        // Buscar ambos usuarios
        Optional<Usuario> usuarioOptA = usuarioRepository.findByEmail("correoprueba1@noenviar.com");
        Optional<Usuario> usuarioOptB = usuarioRepository.findByEmail("correoprueba2@noenviar.com");
        assertTrue(usuarioOptA.isPresent());
        assertTrue(usuarioOptB.isPresent());
        Usuario usuarioA = usuarioOptA.get();
        Usuario usuarioB = usuarioOptB.get();

        // Buscar los tokens generados en email_verificacion
        Optional<EmailVerificacion> tokenOptA = emailVerificacionRepository.findByUsuario_IdUsuario(usuarioA.getIdUsuario());
        Optional<EmailVerificacion> tokenOptB = emailVerificacionRepository.findByUsuario_IdUsuario(usuarioB.getIdUsuario());

        assertTrue(tokenOptA.isPresent(), "Usuario A debe tener un token de verificación");
        assertTrue(tokenOptB.isPresent(), "Usuario B debe tener un token de verificación");

        // Verifica que los códigos son únicos y aleatorios
        String codigoA = tokenOptA.get().getCodigo();
        String codigoB = tokenOptB.get().getCodigo();
        assertNotNull(codigoA, "El código de verificación debe existir para usuario A");
        assertNotNull(codigoB, "El código de verificación debe existir para usuario B");
        assertNotEquals(codigoA, codigoB, "Los códigos de verificación deben ser únicos y aleatorios");
    }
    
    // 17. El email enviado tiene el formato y contenido esperado
    @Test
    void emailEnviado_tieneFormatoYContenidoEsperado() throws Exception {
        // Registrar usuario nuevo
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioTestMail");
        usuarioDTO.setEmail("breakingbenjaminns@gmail.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Mail");
        usuarioDTO.setApellidoResponsable("Test");
        usuarioDTO.setDocumentoResponsable("33333333");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1992, 3, 3));
        usuarioDTO.setGeneroResponsable("MASCULINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());

        registrarUsuarioCompleto(registroDTO);

        // Captura los argumentos del método de envío
        ArgumentCaptor<String> destinatarioCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codigoCaptor = ArgumentCaptor.forClass(String.class);

        // Verifica que se llamó al método de envío
        verify(emailService, times(1)).enviarEmailVerificacion(destinatarioCaptor.capture(), codigoCaptor.capture());

        String destinatario = destinatarioCaptor.getValue();
        String codigo = codigoCaptor.getValue();

        assertEquals("breakingbenjaminns@gmail.com", destinatario);
        assertNotNull(codigo);
        // Puedes agregar más asserts sobre el formato del código, el enlace, etc.
    }
}