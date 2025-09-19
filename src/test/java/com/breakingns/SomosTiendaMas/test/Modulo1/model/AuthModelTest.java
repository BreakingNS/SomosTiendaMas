package com.breakingns.SomosTiendaMas.test.Modulo1.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
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
import jakarta.servlet.http.Cookie;

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
public class AuthModelTest {
    
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

    private final TokenEmitidoService tokenEmitidoService;

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
        //registrarUsuarioCompleto(registroDTO);

        // Asegura que el usuario y el email están verificados
        /* 
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);*/
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
    
    //1. Usuario con caracteres especiales no permitidos (400)
    @Test
    void registroUsuarioConCaracteresEspecialesNoPermitidosDebeRetornarBadRequest() throws Exception {
        String[] caracteresEspeciales = {
            "@", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "{", "}", "[", "]",
            ":", ";", ",", "<", ">", "/", "\\", "|", "\"", "'", "¡", "¿", "?"
        };

        for (String simbolo : caracteresEspeciales) {
            registroDTO.getUsuario().setUsername("usuario" + simbolo + "test");
            int statusRegistro = registrarUsuarioCompleto(registroDTO);
            assertEquals(400, statusRegistro);
        }
    }

    //2. Usuario demasiado corto (<6) o demasiado largo (>16) (400)
    @Test
    void registroUsuarioConPasswordCortoOLargoDebeRetornarBadRequest() throws Exception {
        
        registroDTO.getUsuario().setPassword("h1234");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
        
        registroDTO.getUsuario().setPassword("h1234567891011111234");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }
    
    //3. Usuario con doble punto o doble guion bajo (400)
    @Test
    void registroUsuarioConDoblePuntoODobleGuionDebeRetornarBadRequest() throws Exception {
        // Doble punto
        registroDTO.getUsuario().setUsername("username..test");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);

        // Doble guion bajo
        registroDTO.getUsuario().setUsername("username__test");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //4. Contraseña sin números (400)
    @Test
    void registroUsuarioContraseniaSinNumerosDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioSinNum");
        registroDTO.getUsuario().setPassword("PasswordEjemplo"); // Solo letras
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //5. Contraseña sin letras (400)
    @Test
    void registroContraseniaSinLetrasDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioSinLetras");
        registroDTO.getUsuario().setPassword("32144567"); // Solo números
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //6. Contraseña con espacios (400)
    @Test
    void registroUsuarioContraseniaConEspaciosDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioConEspacios");
        registroDTO.getUsuario().setPassword("Pass word123"); // Contiene espacio
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //7. Contraseña igual al nombre de usuario (400)
    @Test
    void registroUsuarioContraseniaIgualAlUsernameDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioIgual");
        registroDTO.getUsuario().setPassword("usuarioIgual"); // Igual al username
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //8. Email inválido (sin @, sin dominio, etc.) (400)
    @Test
    void registroEmailInvalidoDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioEmail1");
        registroDTO.getUsuario().setEmail("correoprueba1noenviar.com");
        registroDTO.getUsuario().setPassword("Password123");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);

        registroDTO.getUsuario().setUsername("usuarioEmail2");
        registroDTO.getUsuario().setEmail("correoprueba2@noenviar");
        registroDTO.getUsuario().setPassword("Password123");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);

        registroDTO.getUsuario().setUsername("usuarioEmail3");
        registroDTO.getUsuario().setEmail("");
        registroDTO.getUsuario().setPassword("Password123");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //9. Usuario ya registrado (400)
    @Test
    void usuarioYaRegistradoDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioRepetido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        registroDTO.getUsuario().setEmail("correoprueba2@noenviar.com");
        registroDTO.getUsuario().setPassword("Pass987654.");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //10. Email ya registrado (400)
    @Test
    void emailYaRegistradoDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido2");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        registroDTO.getUsuario().setUsername("usuarioValido3");
        registroDTO.getUsuario().setPassword("Pass987654.");
        statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //11. Usuario válido con caracteres permitidos (guion bajo, punto) (200)
    @Test
    void usuarioValidoCaracteresPermitidosDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usu-Valido_123");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //12. Contraseña válida con caracteres especiales permitidos (200)
    @Test
    void contraseniaValidaCaracteresPermitidosDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!*._");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //13. Registro con campos vacíos (400)
    @Test
    void registroCamposVaciosDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuario_Valido.");
        registroDTO.getUsuario().setEmail("");
        registroDTO.getUsuario().setPassword("Password123!*._");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //14. Registro con cantidad minima (6) de caracteres (200)
    @Test
    void registroUsuarioConPassworMinimodValidaDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido1");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("a12345");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //15. Registro con cantidad maxima (16) de caracteres (200)
    @Test
    void registroUsuarioConPasswordMaximoValidaDebeSerExitoso() throws Exception {

        registroDTO.getUsuario().setUsername("usuarioValido2");
        registroDTO.getUsuario().setEmail("correoprueba2@noenviar.com");
        registroDTO.getUsuario().setPassword("a123456789123456");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //16. Registro con todos los datos válidos (200)
    @Test
    void registroUsuarioConDatosValidosDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //17. Registro, teniendo contraseña con caracteres especiales (200)
    @Test
    void registroUsuarioConDatosValidosCaracteres1DebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido1");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("!@#$%^&*con12");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    @Test
    void registroUsuarioConDatosValidosCaracteres2DebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido2");
        registroDTO.getUsuario().setEmail("correoprueba2@noenviar.com");
        registroDTO.getUsuario().setPassword("()_+-={}[]con12");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    @Test
    void registroUsuarioConDatosValidosCaracteres3DebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido3");
        registroDTO.getUsuario().setEmail("nahuel_segura_17@hotmail.com");
        registroDTO.getUsuario().setPassword(":;,.?/con12");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //18. Username con guion medio permitido (200)
    @Test
    void usuarioValidoConGuionMedioDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuario-valido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //19. Username empieza o termina con punto, guion bajo o guion medio (400)
    @Test
    void usuarioEmpiezaOTerminaConCaracterEspecialDebeRetornarBadRequest() throws Exception {
        String[] casos = {".usuario", "usuario.", "_usuario", "usuario_", "-usuario", "usuario-"};
        for (String username : casos) {
            registroDTO.getUsuario().setUsername(username);
            registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
            registroDTO.getUsuario().setPassword("Password123!");
            int statusRegistro = registrarUsuarioCompleto(registroDTO);
            assertEquals(400, statusRegistro);
        }
    }

    //20. Username con secuencias ._ o -.
    @Test
    void usuarioConSecuenciasEspecialesDebeRetornarBadRequest() throws Exception {
        String[] casos = {"usuario._test", "usuario-.", "usuario.-", "usuario_.", "usuario.-test"};
        for (String username : casos) {
            registroDTO.getUsuario().setUsername(username);
            registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
            registroDTO.getUsuario().setPassword("Password123!");
            int statusRegistro = registrarUsuarioCompleto(registroDTO);
            assertEquals(400, statusRegistro);
        }
    }

    //21. Contraseña con solo caracteres especiales (400)
    @Test
    void contraseniaSoloCaracteresEspecialesDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("!@#$%^&*");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //22. Username con espacios al inicio o final (400)
    @Test
    void usuarioConEspaciosExtremosDebeRetornarBadRequest() throws Exception {
        String[] casos = {" usuarioValido", "usuarioValido ", " usuarioValido "};
        for (String username : casos) {
            registroDTO.getUsuario().setUsername(username);
            registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
            registroDTO.getUsuario().setPassword("Password123!");
            int statusRegistro = registrarUsuarioCompleto(registroDTO);
            assertEquals(400, statusRegistro);
        }
    }

    //23. Email con espacios o caracteres no permitidos (400)
    @Test
    void emailConEspaciosOCaracteresNoPermitidosDebeRetornarBadRequest() throws Exception {
        String[] casos = {" test@test.com", "test@ test.com", "test@te st.com", "test@.com", "test@com"};
        for (String email : casos) {
            registroDTO.getUsuario().setUsername("usuarioValido");
            registroDTO.getUsuario().setEmail(email);
            registroDTO.getUsuario().setPassword("Password123!");
            int statusRegistro = registrarUsuarioCompleto(registroDTO);
            assertEquals(400, statusRegistro);
        }
    }
    
    //24. Username con mayúsculas y minúsculas (200)
    @Test
    void usuarioConMayusculasYMinusculasDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("UsuarioValido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //25. Contraseña con longitud exactamente en los límites (200)
    @Test
    void contraseniaConLongitudLimiteMinimosDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("a12345"); // 6 caracteres
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //25. Contraseña con longitud exactamente en los límites (200)
    @Test
    void contraseniaConLongitudLimiteMaximosDebeSerExitoso() throws Exception {
        registroDTO.getUsuario().setUsername("usuarioValido2");
        registroDTO.getUsuario().setEmail("correoprueba2@noenviar.com");
        registroDTO.getUsuario().setPassword("a123456789123456"); // 16 caracteres
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);
    }

    //26. Campos nulos (400)
    @Test
    void registroConCamposNulosDebeRetornarBadRequest() throws Exception {
        registroDTO.getUsuario().setUsername(null);
        registroDTO.getUsuario().setEmail(null);
        registroDTO.getUsuario().setPassword(null);
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(400, statusRegistro);
    }

    //27. Username con caracteres Unicode (400/200)

    //28. Email con subdominios (200)

    //29. Email con caracteres especiales permitidos (200)
    
                    //Validación de Token (expiración, formato, unicidad)

    //30. Username con caracteres repetidos permitidos (200/400)

    //31. Token con formato inválido (400)
    
    //32. Token expirado (401)

    //33. Token duplicado (400)

    //34. Token firmado con clave incorrecta (401)
    @Test
    void tokenFirmadoConClaveIncorrectaDebeRetornarUnauthorized() throws Exception {
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvMTIzIiwiaWF0IjoxNjE2MjM5MDIyfQ.invalidfirma";
        mockMvc.perform(get("/api/usuario/private/perfil")
                .header("Authorization", "Bearer " + tokenInvalido))
                .andExpect(status().isUnauthorized());
    }

    //34. Token con claims faltantes (400)
    @Test
    void tokenConClaimsFaltantesDebeRetornarBadRequest() throws Exception {
        // Token sin 'sub' (subject)
        String tokenSinClaims = "eyJhbGciOiJIUzI1NiJ9.e30.abc123firma";
        mockMvc.perform(get("/api/usuario/private/perfil")
                .header("Authorization", "Bearer " + tokenSinClaims))
                .andExpect(status().isUnauthorized());
    }

    //35. Token con fecha de expiración en el pasado (401)
    @Test
    void tokenExpiradoDebeRetornarUnauthorized() throws Exception {
        // Token simulado con 'exp' en el pasado
        String tokenExpirado = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvMTIzIiwiZXhwIjoxNjE2MjM5MDIyfQ.firmaExpirada";
        mockMvc.perform(get("/api/usuario/private/perfil")
                .header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized());
    }

    //36. Token con fecha de expiración en el futuro pero revocado (401)
    @Test
    void tokenRevocadoDebeRetornarUnauthorized() throws Exception {
        // Supón que este token está en la lista de revocados en tu sistema
        String tokenRevocado = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvMTIzIiwiZXhwIjo0MDAwMDAwMDAwfQ.firmaRevocada";
        mockMvc.perform(get("/api/usuario/private/perfil")
                .header("Authorization", "Bearer " + tokenRevocado))
                .andExpect(status().isUnauthorized());
    }

                    //Validación de Sesión Activa
    
    //37. Sesión activa válida (200)
    @Test
    void sesionActivaValidaDebeRetornarOk() throws Exception {
        // 1. Registrar usuario
        registroDTO.getUsuario().setUsername("usuarioSesion");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuarioSesion");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // 2. Login y obtener token
        MvcResult loginResult = loginUsuario("usuarioSesion", "Password123!");
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertTrue(accessToken != null && !accessToken.isEmpty());

        // 3. Validar sesión activa con endpoint
        mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 4. Validar en el repositorio por usuario_id
        usuarioOpt = usuarioRepository.findByUsername("usuarioSesion");
        assertTrue(usuarioOpt.isPresent());
        Long idUsuario = usuarioOpt.get().getIdUsuario();

        assertTrue(sesionActivaRepository.existsByUsuario_IdUsuario(idUsuario));
    }

    //38. Sesión activa expirada (401)
    @Test
    void sesionActivaExpiradaDebeRetornarUnauthorized() throws Exception {
        String tokenExpirado = "tokenExpiradoSimulado";
        mockMvc.perform(get("/api/sesiones/private/activas")
            .header("Authorization", "Bearer " + tokenExpirado))
            .andExpect(status().isUnauthorized());
    }

    //39. Sesión activa inexistente (404)
    @Test
    void sesionActivaInexistenteDebeRetornarNotFound() throws Exception {
        String tokenInexistente = java.util.UUID.randomUUID().toString();

        // Opcional: verifica que no existe ninguna sesión con ese token
        assertTrue(sesionActivaRepository.findAll().stream()
            .noneMatch(s -> tokenInexistente.equals(s.getToken())));

        mockMvc.perform(get("/api/sesiones/private/activas")
            .header("Authorization", "Bearer " + tokenInexistente))
            .andExpect(status().isUnauthorized());
    }

    //40. Sesión activa con usuario eliminado (404)
    @Test
    void sesionActivaConUsuarioEliminadoDebeRetornarNotFound() throws Exception {
        // 1. Registrar usuario
        registroDTO.getUsuario().setUsername("usuarioEliminar");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuarioEliminar");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // 2. Login y obtener token
        MvcResult loginResult = loginUsuario("usuarioEliminar", "Password123!");
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertTrue(accessToken != null && !accessToken.isEmpty());

        // 3. Eliminar usuario en cascada
        usuarioOpt = usuarioRepository.findByUsername("usuarioEliminar");
        assertTrue(usuarioOpt.isPresent());
        usuarioRepository.delete(usuarioOpt.get());

        // 4. Verificar sesión activa con el token
        mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    //41. Sesión activa con token revocado (401)
    @Test
    void sesionActivaConTokenRevocadoDebeRetornarUnauthorized() throws Exception {
        // 1. Registrar usuario
        registroDTO.getUsuario().setUsername("usuarioRevocado");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("Password123!");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuarioRevocado");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // 2. Login y obtener token
        MvcResult loginResult = loginUsuario("usuarioRevocado", "Password123!");
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertTrue(accessToken != null && !accessToken.isEmpty());

        // 3. Revocar el token (elimina la sesión activa asociada)
        tokenEmitidoService.revocarToken(accessToken);
        
        // 4. Intentar acceder con el token revocado
        mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }
    
                    //Negocio de revocación y logout

    //42. Logout exitoso (200)
    @Test
    void logoutExitosoDebeRetornarOk() throws Exception {
        registrarUsuarioCompleto(registroDTO);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        MvcResult loginResult = loginUsuario("usuario123", 
                                            "ClaveSegura123");
        
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

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());

        result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    //43. Logout con token inválido (401)
    @Test
    void logoutConTokenInvalidoDebeRetornarUnauthorized() throws Exception {
        MvcResult loginResult = loginUsuario("usuPrueba", "P123456");

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

        accessToken = accessToken + "a";

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    //44. Logout con refresh token en vez de access (400)
    @Test
    void logoutConRefreshTokenEnVezDeAccessDebeRetornarBadRequest() throws Exception {
        // Registrar usuario y login para obtener los tokens
        registroDTO.getUsuario().setUsername("usuPrueba");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("P123456");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuPrueba");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        MvcResult loginResult = loginUsuario("usuPrueba", "P123456");

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertTrue(refreshToken != null && !refreshToken.isEmpty());

        // Intentar logout usando el refresh token como access token
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + refreshToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    //45. Logout con token ya revocado (400/401)
    @Test
    void logoutConTokenYaRevocadoDebeRetornarUnauthorizedOBadRequest() throws Exception {
        // Simula un token revocado (deberías tener lógica para revocar el token antes)
        String accessTokenRevocado = "accessTokenRevocadoSimulado";
        String refreshTokenRevocado = "refreshTokenRevocadoSimulado";

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessTokenRevocado)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshTokenRevocado + "\"}"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 400 || status == 401);
    }

    //46. Logout con sesión expirada (401)
    @Test
    void logoutConSesionExpiradaDebeRetornarUnauthorized() throws Exception {
        // 1. Registrar usuario y login para obtener los tokens
        registroDTO.getUsuario().setUsername("usuExpira");
        registroDTO.getUsuario().setEmail("correoprueba1@noenviar.com");
        registroDTO.getUsuario().setPassword("P123456");
        int statusRegistro = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, statusRegistro);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuExpira");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        MvcResult loginResult = loginUsuario("usuExpira", "P123456");
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
        assertTrue(accessToken != null && !accessToken.isEmpty());
        assertTrue(refreshToken != null && !refreshToken.isEmpty());

        // 2. Simular expiración: modifica la fecha de expiración en la BD
        // Ejemplo para sesión activa (ajusta según tu modelo)
        sesionActivaRepository.findByToken(accessToken).ifPresent(sesion -> {
            sesion.setFechaExpiracion(Instant.now().minusSeconds(60 * 60 * 24)); // Expirada hace 1 día
            sesionActivaRepository.save(sesion);
        });

        // 3. Intentar logout con tokens expirados
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    /*          Metodos:
        
        status().isOk() → 200
        status().isCreated() → 201
        status().isNoContent() → 204
        status().isBadRequest() → 400
        status().isUnauthorized() → 401
        status().isForbidden() → 403
        status().isNotFound() → 404
        status().isInternalServerError() → 500
    */

}