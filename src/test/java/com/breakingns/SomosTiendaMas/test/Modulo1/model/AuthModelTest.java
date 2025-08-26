package com.breakingns.SomosTiendaMas.test.Modulo1.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
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
    
    //1. Usuario con caracteres especiales no permitidos (400)
    @Test
    void registroUsuarioConCaracteresEspecialesNoPermitidosDebeRetornarBadRequest() throws Exception {
        String[] caracteresEspeciales = {
            "@", "#", "$", "%", "^", "&", "*", "(", ")", "+", "=", "{", "}", "[", "]",
            ":", ";", ",", "<", ">", "/", "\\", "|", "\"", "'", "¡", "¿", "?"
        };

        for (String simbolo : caracteresEspeciales) {
            int statusRegistro = registrarUsuario("usuario" + simbolo + "test", "Password123", "test" + simbolo + "@test.com");
            assertEquals(400, statusRegistro);
        }
    }

    //2. Usuario demasiado corto (<6) o demasiado largo (>16) (400)
    @Test
    void registroUsuarioConPasswordCortoOLargoDebeRetornarBadRequest() throws Exception {
        
        int statusRegistro = registrarUsuario("usuarioValid", 
                                              "12345", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);

        statusRegistro = registrarUsuario("usuarioValido", 
                                              "12345678912345678", 
                                                       "test2@test.com");
        assertEquals(400, statusRegistro);
    }
    
    //3. Usuario con doble punto o doble guion bajo (400)
    @Test
    void registroUsuarioConDobrePuntoODobleGuion() throws Exception {
        int statusRegistro = registrarUsuario("username..", 
                                              "Password123", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);

        statusRegistro = registrarUsuario("username--", 
                                              "Password123", 
                                                       "test2@test.com");
        assertEquals(400, statusRegistro);
    }

    //4. Contraseña sin números (400)
    @Test
    void registroContraseniaSinNumeros() throws Exception {
        int statusRegistro = registrarUsuario("username", 
                                              "Passwordejemp", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);
    }

    //5. Contraseña sin letras (400)
    @Test
    void registroContraseniaSinLetras() throws Exception {
        int statusRegistro = registrarUsuario("username", 
                                              "32144567", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);
    }

    //6. Contraseña con espacios (400)
    @Test
    void registroUsuarioConUsernameInvalidoDebeRetornarBadRequest() throws Exception {
        int statusRegistro = registrarUsuario("user name", 
                                              "Password123", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);
    }

    //7. Contraseña igual al nombre de usuario (400)
    @Test
    void registroUsuarioContraseniaIguales() throws Exception {
        int statusRegistro = registrarUsuario("username", 
                                              "username", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);
    }

    //8. Email inválido (sin @, sin dominio, etc.) (400)
    @Test
    void registroEmailInvalido() throws Exception {
        int statusRegistro = registrarUsuario("username1", 
                                              "Password123", 
                                                       "test1@@test.com");
        assertEquals(400, statusRegistro);
        
        statusRegistro = registrarUsuario("username2", 
                                              "Password123", 
                                                       "test2test.com");
        assertEquals(400, statusRegistro);

        statusRegistro = registrarUsuario("username3", 
                                              "Password123", 
                                                       "");
        assertEquals(400, statusRegistro);
    }

    //9. Usuario ya registrado (400)
    @Test
    void usuarioYaRegistrado() throws Exception {
        int statusRegistro = registrarUsuario("usuarioRepetido", 
                                              "Password123!", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);
        
        statusRegistro = registrarUsuario("usuarioRepetido", 
                                              "Pass987654.", 
                                                       "test2@test.com");
        assertEquals(400, statusRegistro);
    }

    //10. Email ya registrado (400)
    @Test
    void emailYaRegistrado() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido2", 
                                              "Password123!", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);
        
        statusRegistro = registrarUsuario("usuarioValido2", 
                                              "Pass987654.", 
                                                       "test1@test.com");
        assertEquals(400, statusRegistro);
    }

    //11. Usuario válido con caracteres permitidos (guion bajo, punto) (200)
    @Test
    void usuarioValidoCaracteresPermitidos() throws Exception {
        int statusRegistro = registrarUsuario("usu-Valido_123", 
                                              "Password123!", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);
    }

    //12. Contraseña válida con caracteres especiales permitidos (200)
    @Test
    void contraseniaValidaCaracteresPermitidos() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido", 
                                              "Password123!*._", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);
    }

    //13. Registro con campos vacíos (400)
    @Test
    void registroCamposVacios() throws Exception {
        int statusRegistro = registrarUsuario("usuario_Valido.", 
                                              "Password123!*._", 
                                                       "");
        assertEquals(400, statusRegistro);
    }

    //14. Registro con cantidad minima (6) y maxima (16) de caracteres (200)
    @Test
    void registroUsuarioConPasswordValidaDebeSerExitoso() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido1", 
                                              "a12345", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);

        statusRegistro = registrarUsuario("usuarioValido2", 
                                              "a123456789123456", 
                                                       "test2@test.com");
        assertEquals(200, statusRegistro);
    }

    //15. Registro con todos los datos válidos (200)
    @Test
    void registroUsuarioConDatosValidosDebeSerExitoso() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido", 
                                              "Password123!", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);
    }
    
    //16. Registro, teniendo contraseña con caracteres especiales (200)
    @Test
    void registroUsuarioConDatosValidosCaracteresDebeSerExitoso() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido1", 
                                              "!@#$%^&*con12", 
                                                       "test1@test.com");
        assertEquals(200, statusRegistro);

        statusRegistro = registrarUsuario("usuarioValido2", 
                                              "()_+-={}[]con12", 
                                                       "test2@test.com");
        assertEquals(200, statusRegistro);

        statusRegistro = registrarUsuario("usuarioValido3", 
                                              ":;,.?/con12", 
                                                       "test3@test.com");
        assertEquals(200, statusRegistro);
    }

    //17. Username con guion medio permitido (200)
    @Test
    void usuarioValidoConGuionMedioDebeSerExitoso() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario-valido");
        usuario.setPassword("Password123!");
        usuario.setEmail("test1@test.com");

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isOk());
    }

    //18. Username empieza o termina con punto, guion bajo o guion medio (400)
    @Test
    void usuarioEmpiezaOTerminaConCaracterEspecialDebeRetornarBadRequest() throws Exception {
        String[] casos = {".usuario", "usuario.", "_usuario", "usuario_", "-usuario", "usuario-"};
        for (String username : casos) {
            int statusRegistro = registrarUsuario(username, "Password123!", "test2@test.com");
            assertEquals(400, statusRegistro);
        }
    }

    //19. Username con secuencias ._ o -.
    @Test
    void usuarioConSecuenciasEspecialesDebeRetornarBadRequest() throws Exception {
        String[] casos = {"usuario._test", "usuario-.", "usuario.-", "usuario_.", "usuario.-test"};
        for (String username : casos) {
            int statusRegistro = registrarUsuario(username, "Password123!", "test3@test.com");
            assertEquals(400, statusRegistro);
        }
    }

    //20. Contraseña con solo caracteres especiales (400)
    @Test
    void contraseniaSoloCaracteresEspecialesDebeRetornarBadRequest() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido", "!@#$%^&*", "test4@test.com");
        assertEquals(400, statusRegistro);
    }

    //21. Username con espacios al inicio o final (400)
    @Test
    void usuarioConEspaciosExtremosDebeRetornarBadRequest() throws Exception {
        String[] casos = {" usuarioValido", "usuarioValido ", " usuarioValido "};
        for (String username : casos) {
            int statusRegistro = registrarUsuario(username, "Password123!", "test5@test.com");
            assertEquals(400, statusRegistro);
        }
    }

    //22. Email con espacios o caracteres no permitidos (400)
    @Test
    void emailConEspaciosOCaracteresNoPermitidosDebeRetornarBadRequest() throws Exception {
        String[] casos = {" test@test.com", "test@ test.com", "test@te st.com", "test@.com", "test@com"};
        for (String email : casos) {
            int statusRegistro = registrarUsuario("usuarioValido", "Password123!", email);
            assertEquals(400, statusRegistro);
        }
    }

    //23. Username con mayúsculas y minúsculas (200)
    @Test
    void usuarioConMayusculasYMinusculasDebeSerExitoso() throws Exception {
        int statusRegistro = registrarUsuario("UsuarioValido", "Password123!", "test6@test.com");
        assertEquals(200, statusRegistro);
    }

    //24. Contraseña con longitud exactamente en los límites (200)
    @Test
    void contraseniaConLongitudLimiteDebeSerExitoso() throws Exception {
        int statusRegistro = registrarUsuario("usuarioValido", "a12345", "test7@test.com"); // 6 caracteres
        assertEquals(200, statusRegistro);

        statusRegistro = registrarUsuario("usuarioValido2", "a123456789123456", "test8@test.com"); // 16 caracteres
        assertEquals(200, statusRegistro);
    }

    //25. Campos nulos (400)
    @Test
    void registroConCamposNulosDebeRetornarBadRequest() throws Exception {
        int statusRegistro = registrarUsuario(null, null, null);
        assertEquals(400, statusRegistro);
    }
    
    //26. Username con caracteres Unicode (400/200)

    //27. Email con subdominios (200)

    //28. Email con caracteres especiales permitidos (200)

    //29. Username con caracteres repetidos permitidos (200/400)

                    //Validación de Token (expiración, formato, unicidad)

    //30. Token con formato inválido (401)
    @Test
    void tokenConFormatoInvalidoDebeRetornarBadRequest() throws Exception {
        assertTrue(usuarioRepository.existsByUsername("usuPrueba"));
        loginUsuario("usuPrueba", "P123456");

        String accessToken = "token!@#";

        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    } 

    //31. Token expirado (401)
    @Test
    void tokenExpiradoDebeRetornarUnauthorized() throws Exception {
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

        Thread.sleep(3000);

        MvcResult result = mockMvc.perform(get("/api/sesiones/private/activas")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());

        Thread.sleep(2000);

        result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }
    
    //32. Token duplicado (400)
    @Test
    void tokenDuplicadoDebeRetornarBadRequest() throws Exception {
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

        loginResult = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        cookies = loginResult.getResponse().getCookies();

        MvcResult result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());

        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    //33. Token firmado con clave incorrecta (401)

    //34. Token con claims faltantes (400)

    //35. Token con fecha de expiración en el pasado (401)

    //36. Token con fecha de expiración en el futuro pero revocado (401)

                    //Validación de Sesión Activa
    /*
    //37. Sesión activa válida (200)
    @Test
    void sesionActivaValidaDebeRetornarOk() throws Exception {
        String tokenValido = "tokenValidoSimulado";
        mockMvc.perform(get("/api/sesion/activa")
            .header("Authorization", "Bearer " + tokenValido))
            .andExpect(status().isOk());
    }

    //38. Sesión activa expirada (401)
    @Test
    void sesionActivaExpiradaDebeRetornarUnauthorized() throws Exception {
        String tokenExpirado = "tokenExpiradoSimulado";
        mockMvc.perform(get("/api/sesion/activa")
            .header("Authorization", "Bearer " + tokenExpirado))
            .andExpect(status().isUnauthorized());
    }

    //39. Sesión activa inexistente (404)
    @Test
    void sesionActivaInexistenteDebeRetornarNotFound() throws Exception {
        String tokenInexistente = "tokenInexistenteSimulado";
        mockMvc.perform(get("/api/sesion/activa")
            .header("Authorization", "Bearer " + tokenInexistente))
            .andExpect(status().isNotFound());
    }

    // 40. Sesión activa con usuario eliminado (404)

    // 41. Sesión activa con token revocado (401)
    */
                    //Negocio de revocación y logout

    //42. Logout exitoso (200)
    @Test
    void logoutExitosoDebeRetornarOk() throws Exception {
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

    //45. Logout con token ya revocado (400/401)

    //46. Logout con sesión expirada (401)

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