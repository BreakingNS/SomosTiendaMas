package com.breakingns.SomosTiendaMas.test.Modulo2.features;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenResetPasswordRepository;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*
    PasswordRecoveryTest - flujo de recuperación de contraseña (features)
    - Usa MockMvc para invocar endpoints públicos de recovery/reset.
    - EmailService está mockeado (verificar invocaciones si el método concreto existe).
    - Comentarios TODO indican puntos donde validar tablas tokens_reset_password / banderas de token / sesiones.
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
            "DELETE FROM perfil_empresa",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
})
public class PasswordRecoveryTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final IUsuarioRepository usuarioRepository;
    private final ITokenResetPasswordRepository tokensResetPasswordRepository;
    private final SesionActivaService sesionActivaService;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    @MockBean
    private EmailService emailService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private String registeredEmail;
    private String originalPasswordHash;
    

    @BeforeEach
    void setUp() throws Exception {
        // Registrar un usuario válido para pruebas de recovery
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("recoveryUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Recov");
        usuarioDTO.setApellidoResponsable("User");
        usuarioDTO.setDocumentoResponsable("99999999");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO.setGeneroResponsable("MASCULINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());

        // registrar via endpoint público (igual patrón que Direccion/Telefono tests)
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        Optional<Usuario> uOpt = usuarioRepository.findByUsername("recoveryUser");
        if (uOpt.isPresent()) {
            Usuario u = uOpt.get();
            u.setEmailVerificado(true);
            usuarioRepository.save(u);
            registeredEmail = u.getEmail();
            originalPasswordHash = u.getPassword();
        } else {
            registeredEmail = usuarioDTO.getEmail();
        }

        // reset mock interactions
        Mockito.reset(emailService);
    }

    // Helper: solicitar recuperación (endpoint público)
    private MvcResult requestRecovery(String email) throws Exception {
        Map<String, String> payload = Map.of("email", email);
        return mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andReturn();
    }

    // Helper: realizar reset con token (endpoint público)
    private MvcResult doReset(String token, String nuevaPassword) throws Exception {
        Map<String, String> payload = Map.of("token", token, "nuevaPassword", nuevaPassword);
        return mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andReturn();
    }

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
    }

    // 1) requestRecovery_EmailExistente_CreaTokenYDisparaEmail
    @Test
    void requestRecovery_EmailExistente_CreaTokenYDisparaEmail() throws Exception {
        MvcResult res = requestRecovery(registeredEmail);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Debe responder 2xx para solicitud de recovery");

        // obtener usuario creado
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();

        // DEBUG: listar todo lo que hay en tokens_reset_password
        System.out.println("DEBUG: tokens_reset_password.findAll():");
        tokensResetPasswordRepository.findAll().forEach((TokenResetPassword t) -> {
            System.out.println(" token.id=" + t.getId()
                + " token.token=" + t.getToken()
                + " usuario_id=" + (t.getUsuario()!=null ? t.getUsuario().getIdUsuario() : "null")
                + " usado=" + t.isUsado()
                + " creado=" + t.getFechaCreacion());
                });

        // DEBUG: consultar por usuario id directamente (si el repo devuelve Optional)
        Object raw = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario());
        System.out.println("DEBUG: raw findByUsuario_IdUsuarioAndUsadoFalse -> " + raw);

        // Alternativa si el repo tiene findByUsuario_IdUsuario (sin usado)
        try {
            var maybe = tokensResetPasswordRepository.findByUsuario_IdUsuario(u.getIdUsuario());
            System.out.println("DEBUG: findByUsuario_IdUsuario -> " + maybe);
        } catch (Exception ex) {
            System.out.println("DEBUG: findByUsuario_IdUsuario threw: " + ex);
        }
        // leer token de la tabla tokens_reset_password (repo debe devolver Optional<TokenResetPassword>)
        Optional<com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword> optToken =
            tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario());

        assertTrue(optToken.isPresent(), "Debe haberse creado un registro de token de recuperación para el usuario");
        String codigo = optToken.get().getToken();

        org.mockito.ArgumentCaptor<String> toCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);

        // verificar que se llamó al método correcto con destinatario y token
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(1))
            .enviarEmailRecuperacionPassword(toCaptor.capture(), tokenCaptor.capture());

        String to = toCaptor.getValue();
        String tokenEnviado = tokenCaptor.getValue();

        System.out.println(">>> codigoBD = [" + codigo + "]");
        System.out.println(">>> tokenEnviado = [" + tokenEnviado + "]");

        assertTrue(registeredEmail.equals(to), "Email debe haberse enviado al usuario");
        assertTrue(tokenEnviado != null && !tokenEnviado.isBlank(), "Debe haberse enviado un token no vacío");
        assertTrue(tokenEnviado.equals(codigo) || tokenEnviado.contains(codigo),
            "El token enviado por EmailService debe corresponder al código generado en BD");
    }
    
    // 2) requestRecovery_EmailInexistente_NoRevelaYNoCreaToken
    @Test
    void requestRecovery_EmailInexistente_NoRevelaYNoCreaToken() throws Exception {
        MvcResult res = requestRecovery("correoprueba1@noenviar.com");
        int status = res.getResponse().getStatus();
        // política típica: responder 200 para no revelar existencia
        assertTrue(status >= 200 && status < 300, "No debe revelar existencia de cuenta (responder 2xx)");

        // comprobar que no se creó ningún token en la tabla tokens_reset_password
        var all = tokensResetPasswordRepository.findAll();
        assertTrue(all == null || all.isEmpty(), "No debe haberse creado ningún token para un email inexistente");

        // verificar que no se envió ningún email de recuperación
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.never())
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString());
    }
    
    @Test
    void requestRecovery_EmailNoVerificado_ComportamientoSegunPolicy() throws Exception {
        // crear un usuario nuevo NO verificado
        RegistroUsuarioDTO u2 = new RegistroUsuarioDTO();
        u2.setUsername("noVerified");
        u2.setEmail("correoprueba2@noenviar.com");
        u2.setPassword("ClaveSegura123");
        u2.setNombreResponsable("NV");
        u2.setApellidoResponsable("User");
        u2.setDocumentoResponsable("77777777");
        u2.setTipoUsuario("PERSONA_FISICA");
        u2.setAceptaTerminos(true);
        u2.setAceptaPoliticaPriv(true);
        u2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        u2.setGeneroResponsable("MASCULINO");
        u2.setIdioma("es");
        u2.setTimezone("America/Argentina/Buenos_Aires");
        u2.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO reg2 = new RegistroUsuarioCompletoDTO();
        reg2.setUsuario(u2); reg2.setDirecciones(List.of()); reg2.setTelefonos(List.of());
        MvcResult regResult = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reg2)))
            .andReturn();
        int regStatus = regResult.getResponse().getStatus();
        System.out.println("DEBUG: registro noVerified status=" + regStatus + " response=" + regResult.getResponse().getContentAsString());
        assertTrue(regStatus >= 200 && regStatus < 300, "El registro de prueba debe responder 2xx");

        // localizar usuario recién creado por email y comprobar que NO está verificado
        String testEmail = u2.getEmail();
        Usuario unv = usuarioRepository.findByEmail(testEmail)
            .orElseThrow(() -> new IllegalStateException("Usuario noVerified no encontrado tras registro"));
        assertTrue(!Boolean.TRUE.equals(unv.getEmailVerificado()), "El usuario debe estar NO verificado para este test");

        // solicitar recuperación (el controller debe devolver 200 aunque el service lance EmailNoVerificadoException)
        MvcResult res = requestRecovery(testEmail);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "El endpoint público debe responder 2xx aunque el email no esté verificado");

        // comprobar que no se creó token para este usuario
        boolean existeTokenParaUsuario = tokensResetPasswordRepository.findAll().stream()
            .anyMatch(t -> t.getUsuario() != null && unv.getIdUsuario().equals(t.getUsuario().getIdUsuario()));
        assertTrue(!existeTokenParaUsuario, "No debería crearse token de recuperación para un email no verificado");

        // verificar que no se envió email de recuperación
        Mockito.verify(emailService, Mockito.never())
            .enviarEmailRecuperacionPassword(Mockito.anyString(), Mockito.anyString());
    }

    // 4) requestRecovery_RateLimit_TooManyRequests
    @Test
    void requestRecovery_RateLimit_TooManyRequests() throws Exception {
        // Enviar múltiples solicitudes seguidas
        int attempts = 5;
        for (int i = 0; i < attempts; i++) {
            requestRecovery(registeredEmail);
        }

        // Verificar tokens del usuario: debe haber exactamente 1 activo (usado=false)
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        long activos = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .filter(t -> !t.isUsado())
            .count();

        assertTrue(activos == 1, "Debe existir exactamente un token activo pese a múltiples solicitudes");

        // Verificar que el servicio de email fue invocado (una vez por solicitud o según política)
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.atLeastOnce())
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), org.mockito.Mockito.anyString());

        // Si quieres verificar llamadas exactas (reenvío por cada intento), usa:
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(attempts))
             .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), org.mockito.Mockito.anyString());
    }

    // 5) generarMultipleTokens_UsoDelMasRecienteValido
    @Test
    void generarMultipleTokens_UsoDelMasRecienteValido() throws Exception {
        // Primer y segundo request
        requestRecovery(registeredEmail);
        requestRecovery(registeredEmail);

        // Capturar ambos envíos de email
        org.mockito.ArgumentCaptor<String> toCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.atLeast(2))
            .enviarEmailRecuperacionPassword(toCaptor.capture(), tokenCaptor.capture());

        // Tomar el último token enviado
        String ultimoTokenEnviado = tokenCaptor.getAllValues().get(tokenCaptor.getAllValues().size() - 1);

        // Buscar el token más reciente en BD para el usuario
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        TokenResetPassword ultimo = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .max((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()))
            .orElse(null);

        assertTrue(ultimo != null, "Debe existir al menos un token en la BD");
        // Con la política de reuso, el último token enviado debe coincidir con el token en BD
        assertTrue(ultimoTokenEnviado.equals(ultimo.getToken()) || ultimoTokenEnviado.contains(ultimo.getToken()),
            "El último token enviado debe corresponder al token más reciente creado/en BD");

        // Validar que como mucho exista 1 token activo (según política)
        long activos = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .filter(t -> !t.isUsado())
            .count();
        assertTrue(activos <= 1, "No deberían quedar múltiples tokens activos para el mismo usuario");
    }
    
    // 6) resetPassword_TokenValido_CambiaPassword_MarcaTokenUsado
    @Test
    void resetPassword_TokenValido_CambiaPassword_MarcaTokenUsado() throws Exception {
        // 1) solicitar recovery para obtener token (se asume que se guarda en BD)
        requestRecovery(registeredEmail);

        // localizar usuario por email (más robusto) y leer token activo en BD
        Usuario usu = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        Optional<com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword> optToken =
            tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(usu.getIdUsuario());

        assertTrue(optToken.isPresent(), "Debe haberse creado un token de recovery en BD antes de reset");
        String token = optToken.get().getToken();

        // 2) ejecutar reset con token válido
        MvcResult res = doReset(token, "nuevaContra12");
        int status = res.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Reset exitoso debe devolver 2xx");

        // 3) comprobar que la contraseña del usuario cambió (hash distinto)
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        assertNotEquals(originalPasswordHash, u.getPassword(), "La contraseña debe haberse actualizado");

        // 4) comprobar que el token fue marcado como usado en BD (o eliminado según tu implementación)
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> token.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent(), "El token usado debe seguir existiendo (o ajustar si lo eliminas)");
        assertTrue(maybe.get().isUsado(), "El token debe marcarse como usado tras el reset");

        // TODO: si tu lógica invalida sesiones/refresh tokens, añadir comprobaciones aquí
    }
    
    // 7) resetPassword_TokenExpirado_Error
    @Test
    void resetPassword_TokenExpirado_Error() throws Exception {
        // insertar token expirado en BD para el usuario registrado
        Usuario usu = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        String expiredToken = "EXPIRED_TOKEN_12345";

        com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword tokenExp = 
            new com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword();
        tokenExp.setToken(expiredToken);
        tokenExp.setUsuario(usu);
        tokenExp.setUsado(false);
        tokenExp.setFechaCreacion(java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.MINUTES));
        tokenExp.setFechaExpiracion(java.time.Instant.now().minus(15, java.time.temporal.ChronoUnit.MINUTES));
        tokensResetPasswordRepository.save(tokenExp);

        // intentar reset con token expirado
        MvcResult res = doReset(expiredToken, "OtraClave123");
        int status = res.getResponse().getStatus();

        // según convención esperar 4xx (400/401/410). Ajustar si tu API devuelve otro código.
        assertTrue(status >= 400 && status < 500, "Token expirado debe devolver 4xx");

        // comprobar estado en BD: el token debe existir y no haberse marcado como usado por un intento fallido
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> expiredToken.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent(), "El token expirado debe existir en la BD para el test");
        assertTrue(!maybe.get().isUsado(), "El token expirado no debe marcarse como usado tras intento fallido");
    }
    
    // 8) resetPassword_TokenReusado_Error
    @Test
    void resetPassword_TokenReusado_Error() throws Exception {
        // generar token válido vía requestRecovery
        requestRecovery(registeredEmail);

        Usuario usu = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        Optional<TokenResetPassword> opt = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(usu.getIdUsuario());
        assertTrue(opt.isPresent(), "Debe existir token antes de usarlo");
        String token = opt.get().getToken();

        // primer uso: debe cambiar la contraseña (éxito 2xx)
        MvcResult r1 = doReset(token, "PrimeraPass123!");
        int s1 = r1.getResponse().getStatus();
        assertTrue(s1 >= 200 && s1 < 300, "Primer uso con token válido debe devolver 2xx");

        // comprobar que el token quedó marcado como usado en BD
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> token.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent(), "Token usado debe existir en BD");
        assertTrue(maybe.get().isUsado(), "Token debe marcarse como usado tras primer uso");

        // almacenar password tras primer uso
        String passAfterFirst = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();

        // segundo uso: debe fallar (4xx) y NO modificar la contraseña
        MvcResult r2 = doReset(token, "SegundaPass123!");
        int s2 = r2.getResponse().getStatus();
        assertTrue(s2 >= 400 && s2 < 500, "Reusar token debe devolver 4xx");

        String passAfterSecond = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();
        assertTrue(passAfterFirst.equals(passAfterSecond), "Segundo intento con token reusado no debe cambiar la contraseña");
    }

    // 9) resetPassword_TokenInvalido_Error
    @Test
    void resetPassword_TokenInvalido_Error() throws Exception {
        // guardar hash actual para comprobar que no cambia
        String before = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();

        MvcResult res = doReset("TOKEN_NO_EXISTE_123", "Pass12345");
        int status = res.getResponse().getStatus();
        assertTrue(status >= 400 && status < 500, "Token inválido debe devolver 4xx");

        // comprobar que la contraseña del usuario no se modificó
        String after = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();
        assertTrue(before.equals(after), "Intento con token inválido no debe cambiar la contraseña");
    }
    
    // 10) resetPassword_PasswordDebil_ValidaYRechaza
    @Test
    void resetPassword_PasswordDebil_ValidaYRechaza() throws Exception {
        // preparar token válido via requestRecovery
        requestRecovery(registeredEmail);

        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        Optional<TokenResetPassword> opt = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario());
        assertTrue(opt.isPresent(), "Debe existir token válido en BD antes del reset débil");
        String token = opt.get().getToken();

        // guardar hash previo para comprobar que no cambia
        String beforeHash = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();

        // intentar reset con contraseña débil
        MvcResult res = doReset(token, "123"); // contraseña débil
        int status = res.getResponse().getStatus();

        // esperar fallo de validación (4xx)
        assertTrue(status >= 400 && status < 500, "Contraseña débil debe producir 4xx");

        // verificar que la contraseña no se modificó
        String afterHash = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();
        assertTrue(beforeHash.equals(afterHash), "La contraseña no debe cambiar si la validación falla");

        // verificar que el token NO fue marcado como usado por intento inválido
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> token.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent(), "El token de prueba debe existir en BD");
        assertTrue(!maybe.get().isUsado(), "Intento con contraseña débil no debe marcar el token como usado");
    }

    // 11) resetPassword_Concurrencia_UnoExitosoOtroFalla
    @Test
    void resetPassword_Concurrencia_UnoExitosoOtroFalla() throws Exception {
        // generar token válido
        requestRecovery(registeredEmail);

        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        Optional<TokenResetPassword> opt = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario());
        assertTrue(opt.isPresent(), "Debe existir token antes de la prueba de concurrencia");
        String token = opt.get().getToken();

        // preparar concurrencia: dos tareas que intentan usar el mismo token simultáneamente
        final java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);
        java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newFixedThreadPool(2);

        java.util.concurrent.Callable<Integer> taskA = () -> {
            barrier.await();
            MvcResult r = doReset(token, "ConcurPassA1!");
            return r.getResponse().getStatus();
        };
        java.util.concurrent.Callable<Integer> taskB = () -> {
            barrier.await();
            MvcResult r = doReset(token, "ConcurPassB2!");
            return r.getResponse().getStatus();
        };

        var f1 = ex.submit(taskA);
        var f2 = ex.submit(taskB);

        int s1 = f1.get(10, java.util.concurrent.TimeUnit.SECONDS);
        int s2 = f2.get(10, java.util.concurrent.TimeUnit.SECONDS);

        ex.shutdownNow();

        // exactamente una debe ser 2xx y la otra 4xx (ajustar si tu API usa otro código)
        boolean aSuccess = (s1 >= 200 && s1 < 300);
        boolean bSuccess = (s2 >= 200 && s2 < 300);
        boolean aFail = (s1 >= 400 && s1 < 500);
        boolean bFail = (s2 >= 400 && s2 < 500);

        assertTrue((aSuccess && bFail) || (bSuccess && aFail),
            "En concurrencia, sólo una petición debe tener éxito y la otra fallar (uno 2xx y otro 4xx). s1=" + s1 + " s2=" + s2);

        // token debe quedar marcado como usado
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> token.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent(), "El token debe existir en BD");
        assertTrue(maybe.get().isUsado(), "El token debe marcarse como usado tras una operación exitosa");

        // comprobar que la contraseña cambió respecto al original (no comprobamos exactamente cuál de las dos)
        String afterHash = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();
        assertTrue(!originalPasswordHash.equals(afterHash), "La contraseña debe haberse actualizado por la petición exitosa");
    }
    
    // 12) usarTokenParaOtroUsuario_Error
    @Test
    void usarTokenParaOtroUsuario_Error() throws Exception {
        // crear un segundo usuario verificado (user B)
        RegistroUsuarioDTO u2 = new RegistroUsuarioDTO();
        u2.setUsername("otherUser");
        u2.setEmail("correoprueba1@noenviar.com");
        u2.setPassword("ClaveSegura123");
        u2.setNombreResponsable("Other");
        u2.setApellidoResponsable("User");
        u2.setDocumentoResponsable("11111111");
        u2.setTipoUsuario("PERSONA_FISICA");
        u2.setAceptaTerminos(true);
        u2.setAceptaPoliticaPriv(true);
        u2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        u2.setGeneroResponsable("MASCULINO");
        u2.setIdioma("es");
        u2.setTimezone("America/Argentina/Buenos_Aires");
        u2.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO regB = new RegistroUsuarioCompletoDTO();
        regB.setUsuario(u2); regB.setDirecciones(List.of()); regB.setTelefonos(List.of());
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regB)))
            .andReturn();

        // marcar B como verificado (buscamos por email y actualizamos)
        Usuario userB = usuarioRepository.findByEmail(u2.getEmail()).orElseThrow();
        userB.setEmailVerificado(true);
        usuarioRepository.save(userB);
        String beforeHashB = userB.getPassword();

        // generar token para user A (registeredEmail)
        requestRecovery(registeredEmail);
        Usuario userA = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        String tokenA = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(userA.getIdUsuario())
            .orElseThrow().getToken();

        // usar tokenA para reset (el endpoint no recibe email, por tanto afectará al dueño del token)
        MvcResult r = doReset(tokenA, "CambioParaA123!");
        int status = r.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Reset con token válido debe permitir cambiar password del propietario del token");

        // comprobar que B NO fue afectado
        String afterHashB = usuarioRepository.findByEmail(userB.getEmail()).orElseThrow().getPassword();
        assertTrue(beforeHashB.equals(afterHashB), "El token de A no debe cambiar la contraseña de otro usuario (B)");
    }

    // 12b) usarTokenParaOtroUsuario_Seguridad_VerificarNoAfectaB (refuerzo del test actual)
    @Test
    void usarTokenParaOtroUsuario_Seguridad_VerificarNoAfectaB() throws Exception {
        // crear y verificar usuario B (ya lo haces en el test 12)
        RegistroUsuarioDTO u2 = new RegistroUsuarioDTO();
        u2.setUsername("otherUser2");
        u2.setEmail("other2@noenviar.com");
        u2.setPassword("ClaveSegura123");
        u2.setNombreResponsable("Other2");
        u2.setApellidoResponsable("User2");
        u2.setDocumentoResponsable("22222222");
        u2.setTipoUsuario("PERSONA_FISICA");
        u2.setAceptaTerminos(true);
        u2.setAceptaPoliticaPriv(true);
        u2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        u2.setGeneroResponsable("MASCULINO");
        u2.setIdioma("es");
        u2.setTimezone("America/Argentina/Buenos_Aires");
        u2.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO regB = new RegistroUsuarioCompletoDTO();
        regB.setUsuario(u2); regB.setDirecciones(List.of()); regB.setTelefonos(List.of());
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(regB)))
            .andReturn();

        Usuario userB = usuarioRepository.findByEmail(u2.getEmail()).orElseThrow();
        userB.setEmailVerificado(true);
        usuarioRepository.save(userB);
        String beforeHashB = userB.getPassword();

        // generar token para user A (registeredEmail)
        requestRecovery(registeredEmail);
        Usuario userA = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        String tokenA = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(userA.getIdUsuario())
            .orElseThrow().getToken();

        // guardar hash previo de A
        String beforeHashA = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();

        // usar tokenA para reset -> debería cambiar A
        MvcResult r = doReset(tokenA, "CambioParaA123!");
        int status = r.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Reset con token válido debe permitir cambiar password del propietario del token");

        // comprobar que A se cambió
        String afterHashA = usuarioRepository.findByEmail(registeredEmail).orElseThrow().getPassword();
        assertNotEquals(beforeHashA, afterHashA, "La contraseña de A debe haberse actualizado");

        // comprobar token marcado como usado
        var maybeToken = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> tokenA.equals(t.getToken()))
            .findFirst().orElseThrow();
        assertTrue(maybeToken.isUsado(), "El tokenA debe quedar marcado como usado");

        // comprobar que B NO fue afectado
        String afterHashB = usuarioRepository.findByEmail(userB.getEmail()).orElseThrow().getPassword();
        assertTrue(beforeHashB.equals(afterHashB), "El token de A no debe cambiar la contraseña de otro usuario (B)");
    }
    
    // 13) tokenCaducadoYNuevoToken_CrearNuevoValido
    @Test
    void tokenCaducadoYNuevoToken_CrearNuevoValido() throws Exception {
        // obtener usuario
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();

        // insertar token expirado manualmente
        String oldToken = "EXPIRED_FOR_RENEW_999";
        com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword expired = 
            new com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword();
        expired.setToken(oldToken);
        expired.setUsuario(u);
        expired.setUsado(false);
        expired.setFechaCreacion(Instant.now().minus(30, ChronoUnit.MINUTES));
        expired.setFechaExpiracion(Instant.now().minus(10, ChronoUnit.MINUTES));
        tokensResetPasswordRepository.save(expired);

        // solicitar recovery -> el servicio debe detectar token expirado y crear uno nuevo + enviar email
        MvcResult res = requestRecovery(registeredEmail);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Solicitud de recovery debe responder 2xx");

        // debe existir al menos un token no usado y no expirado
        var maybeNew = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .filter(t -> !t.isUsado())
            .filter(t -> t.getFechaExpiracion() != null && t.getFechaExpiracion().isAfter(Instant.now()))
            .findFirst();
        assertTrue(maybeNew.isPresent(), "Debe haberse creado un nuevo token válido tras detectar expirado");

        String nuevoToken = maybeNew.get().getToken();

        // verificar que EmailService fue invocado con el nuevo token
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.atLeastOnce())
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), tokenCaptor.capture());
        assertTrue(tokenCaptor.getAllValues().stream().anyMatch(v -> v != null && v.contains(nuevoToken)),
            "El email enviado debe contener el nuevo token generado");

        // usar el nuevo token para resetear contraseña (debe funcionar)
        MvcResult resetRes = doReset(nuevoToken, "NuevaValida123!");
        int resetStatus = resetRes.getResponse().getStatus();
        assertTrue(resetStatus >= 200 && resetStatus < 300, "Reset con nuevo token válido debe devolver 2xx");

        // comprobar que el nuevo token quedó marcado como usado
        var used = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> nuevoToken.equals(t.getToken()))
            .findFirst().orElseThrow();
        assertTrue(used.isUsado(), "El nuevo token debe quedar marcado como usado tras el reset exitoso");
    }
    
    // 14) sideEffects_AlResetar_RevocarSesionesYRefreshTokens (esqueleto con guía)
    @Test
    void sideEffects_AlResetar_RevocarSesionesYRefreshTokens() throws Exception {
        // usar el usuario creado en setUp
        Usuario usuario = usuarioRepository.findByEmail(registeredEmail).orElseThrow();

        // hacer login para simular sesión activa y obtener efectos
        loginUsuario(usuario.getUsername(), "ClaveSegura123");

        // comprobar que hay sesiones antes del reset (usar el servicio o repos directos)
        List<SesionActivaDTO> sesionesAntes = sesionActivaService.listarSesionesActivasParaTests(usuario.getIdUsuario());
        assertFalse(sesionesAntes.isEmpty(), "Usuario debe tener sesión activa tras login");

        // solicitar recovery y verificar email enviado
        requestRecovery(registeredEmail);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.atLeastOnce())
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), org.mockito.Mockito.anyString());

        // localizar token y hacer reset
        Optional<TokenResetPassword> optToken = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(usuario.getIdUsuario());
        assertTrue(optToken.isPresent(), "Debe existir token de recovery antes del reset");
        String token = optToken.get().getToken();

        MvcResult res = doReset(token, "nuevaContra12");
        int status = res.getResponse().getStatus();
        assertTrue(status >= 200 && status < 300, "Reset exitoso debe devolver 2xx");

        // comprobar cambio de contraseña
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        assertNotEquals(originalPasswordHash, u.getPassword(), "La contraseña debe haberse actualizado");

        // comprobar que token quedó marcado como usado
        var maybe = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> token.equals(t.getToken()))
            .findFirst();
        assertTrue(maybe.isPresent());
        assertTrue(maybe.get().isUsado(), "El token debe marcarse como usado tras reset");

        // comprobar revocación de sesiones/tokens usando repositorios o servicios (según infra)
        List<SesionActivaDTO> sesionesDespues = sesionActivaService.listarSesionesActivasParaTests(usuario.getIdUsuario());
        // cada sesión asociada al usuario debe estar marcada como revocada
        for (SesionActivaDTO s : sesionesDespues) {
            // ajustar a getRevocado() si tu DTO no tiene isRevocado()
            assertTrue(Boolean.TRUE.equals(s.isRevocado()), "Cada sesión debe estar revocada tras reset");
        }
 
        List<TokenEmitido> tokensEmitidos = tokenEmitidoRepository.findAllByUsuario_Username(usuario.getUsername());
        for (TokenEmitido t : tokensEmitidos) {
            assertTrue(Boolean.TRUE.equals(t.isRevocado()), "Cada token emitido debe estar marcado como revocado tras reset");
        }

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUsuario_Username(usuario.getUsername());
        for (RefreshToken r : refreshTokens) {
            assertTrue(Boolean.TRUE.equals(r.getRevocado()), "Cada refresh token debe estar marcado como revocado tras reset");
        }
    }
    
    // 15) emailContenido_ContieneLinkConToken (esqueleto)
    @Test
    void emailContenido_ContieneLinkConToken() throws Exception {
        // solicitar recovery
        requestRecovery(registeredEmail);

        // capturar token pasado al EmailService mock
        org.mockito.ArgumentCaptor<String> toCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(1))
            .enviarEmailRecuperacionPassword(toCaptor.capture(), tokenCaptor.capture());

        String destinatario = toCaptor.getValue();
        String tokenEnviado = tokenCaptor.getValue();

        // comprobar destinatario
        assertTrue(registeredEmail.equals(destinatario), "Email enviado al destinatario esperado");

        // comprobar que el token enviado coincide con el token almacenado en BD
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        Optional<TokenResetPassword> opt = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario());
        assertTrue(opt.isPresent(), "Debe existir token en BD");
        String tokenBD = opt.get().getToken();

        // el EmailService puede enviar el token tal cual o envolverlo en un link; aceptamos ambas formas
        assertTrue(tokenEnviado.equals(tokenBD) || tokenEnviado.contains(tokenBD),
            "El token enviado por EmailService debe corresponder al token generado en BD");
    }
    
    // 16) requestRecovery_ReusaTokenSiNoExpirado
    @Test
    void requestRecovery_ReusaTokenSiNoExpirado() throws Exception {
        // primer request -> crea token
        requestRecovery(registeredEmail);
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        TokenResetPassword first = tokensResetPasswordRepository.findByUsuario_IdUsuarioAndUsadoFalse(u.getIdUsuario())
            .orElseThrow();
        String token1 = first.getToken();

        // segundo request -> según política debe reusar el token (no crear otro activo)
        // reseteo las interacciones para verificar sólo la segunda invocación si hace falta
        Mockito.reset(emailService);
        requestRecovery(registeredEmail);

        // capturar token enviado en la segunda invocación
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(1))
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), tokenCaptor.capture());
        String tokenEnviado = tokenCaptor.getValue();

        // debe ser el mismo token (o contenerlo si el servicio envía un link)
        assertTrue(tokenEnviado.equals(token1) || tokenEnviado.contains(token1),
            "El token reenviado debe ser el token no expirado existente");

        // debe existir exactamente un token activo para el usuario
        long activos = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .filter(t -> !t.isUsado())
            .count();
        assertTrue(activos == 1, "Debe existir exactamente un token activo (reuso) para el usuario");
    }

    // 17) requestRecovery_Expirado_CreaNuevoToken
    @Test
    void requestRecovery_Expirado_CreaNuevoToken() throws Exception {
        // crear token expirado manualmente
        Usuario u = usuarioRepository.findByEmail(registeredEmail).orElseThrow();
        String oldToken = "EXPIRED_FOR_POLICY_001";
        TokenResetPassword expired = new TokenResetPassword();
        expired.setToken(oldToken);
        expired.setUsuario(u);
        expired.setUsado(false);
        expired.setFechaCreacion(Instant.now().minus(30, ChronoUnit.MINUTES));
        expired.setFechaExpiracion(Instant.now().minus(1, ChronoUnit.MINUTES));
        tokensResetPasswordRepository.save(expired);

        // resetear interacciones y solicitar recovery -> debe crearse uno nuevo y enviarse
        Mockito.reset(emailService);
        requestRecovery(registeredEmail);

        // capturar token enviado
        org.mockito.ArgumentCaptor<String> tokenCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(1))
            .enviarEmailRecuperacionPassword(org.mockito.Mockito.eq(registeredEmail), tokenCaptor.capture());
        String tokenEnviado = tokenCaptor.getValue();

        // el token enviado no debe coincidir con el token expirado; además debe existir en BD y no estar expirado
        assertTrue(tokenEnviado != null && !tokenEnviado.isBlank(), "Debe enviarse un token no vacío");
        assertTrue(!tokenEnviado.equals(oldToken) && !tokenEnviado.contains(oldToken),
            "Debe haberse generado un token distinto al expirado");

        var maybeNew = tokensResetPasswordRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && u.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
            .filter(t -> !t.isUsado())
            .filter(t -> t.getFechaExpiracion() != null && t.getFechaExpiracion().isAfter(Instant.now()))
            .findFirst();
        assertTrue(maybeNew.isPresent(), "Debe haberse creado un nuevo token válido no expirado en BD");
        String tokenBD = maybeNew.get().getToken();
        assertTrue(tokenEnviado.equals(tokenBD) || tokenEnviado.contains(tokenBD),
            "El token enviado debe corresponder al token recién creado en BD");
    }

}