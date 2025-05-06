package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.controller.AuthController;
import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.ChangePasswordRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.OlvidePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.dto.ResetPasswordRequest;
import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenResetPasswordRepository;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.SqlGroup;

/*                                                  AuthPasswordControllerSecurityTest
                            /public/olvide-password

        1) Solicitud válida sin autenticación

        2) Email inválido (malformado)

        3) Email no registrado

        4) Acceso con token válido (opcional)
    
                            /public/reset-password

        5) Token válido y nueva contraseña válida

        6) Token inválido (cualquier string)

        7) Token expirado 

        8) Token usado

        9) Contraseña con validación fallida (demasiado corta)

        10) Contraseña con validación fallida (demasiado larga)

        11) Token válido pero para otro usuario
    
                            /public/change-password

        12) Token válido, contraseña actual correcta

        13) Token válido, contraseña actual incorrecta

        14) Token vencido o inválido (JWT)

        15) Token válido pero con rol no permitido (otro rol ficticio o no incluido)
        Nota: No se testea el acceso con rol no permitido, ya que los usuarios sin roles
        no pueden autenticarse y obtener un token válido (ver test de login sin roles).

        16) Sin token

        17) Contraseña nueva inválida

        18) Cambiar contraseña con mismo valor que actual (opcional según reglas)
    
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
public class AuthPasswordControllerSecurityTest {
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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    private final AuthController authController;
    
    private final SesionActivaService sesionActivaService;
    private final UsuarioServiceImpl usuarioService;
    private final RolService rolService;
    
    private final IUsuarioRepository usuarioRepository;
    private final ITokenResetPasswordRepository tokenResetPasswordRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    
    private String refreshAdmin;
    private String refreshUsuario;
    private String tokenAdmin;
    private String tokenUsuario;
    private Long idAdmin;
    private Long idUsuario;
    
    @BeforeEach
    void setUp() throws Exception {
        // Siempre registrar y loguear, sin condicionales
        registrarAdmin("admin", "987654", "admin@test.com");
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

        mockMvc.perform(post("/api/registro/public/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usuario)))
            .andExpect(status().isOk());
    }
    
    // Método para registrar un admin
    private void registrarAdmin(String username, String password, String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setEmail(email);

        mockMvc.perform(post("/api/registro/public/admin")
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
    
    //                                      /public/olvide-password
    // 1) Solicitud válida sin autenticación
    @Test
    void solicitarRecuperacionPassword_solicitudValidaSinAutenticacion_retornaOk() throws Exception {
        OlvidePasswordRequest request = new OlvidePasswordRequest("usuario@test.com");

        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
    
    // 2) Email inválido (malformado)
    @Test
    void solicitarRecuperacionPassword_emailInvalido_retornarBadRequest() throws Exception {
        OlvidePasswordRequest request = new OlvidePasswordRequest("email-invalido"); // sin @, sin dominio

        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Formato de email inválido."));
    }
    
    // 3) Email no registrado
    @Test
    void solicitarRecuperacionPassword_emailNoRegistrado_retornarOk() throws Exception {
        OlvidePasswordRequest request = new OlvidePasswordRequest("noexiste@test.com");

        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
    
    // 4) Acceso con token válido (opcional)
    @Test
    void solicitarRecuperacionPassword_conTokenValido_retornarOk() throws Exception {
        OlvidePasswordRequest request = new OlvidePasswordRequest("usuario@test.com");

        mockMvc.perform(post("/api/password/public/olvide-password")
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
    
    //                                      /public/reset-password
    // 5) Token válido y nueva contraseña válida
    @Test
    void resetPassword_conTokenValidoYCambioCorrecto_retornarOk() throws Exception {
        // 1. Registrar el usuario previamente (ya lo hiciste antes)
        String email = "usuPrueba@test.com";
        registrarUsuario("usuPrueba", "abc123456", email);
        
        // 2. Simular solicitud de recuperación de contraseña
        OlvidePasswordRequest request = new OlvidePasswordRequest(email);
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // 3. Buscar el token generado en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        TokenResetPassword tokenReset = passwordResetTokenRepository
            .findByUsuarioOrderByFechaExpiracionDesc(usuario)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Token no generado"));

        // 4. Enviar contraseña inválida con el token correcto
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            tokenReset.getToken(), "contraseniaOk" // Contraseña valida
        );

        mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isOk());
    }
    
    // 6) Token inválido (cualquier string)
    @Test
    void resetPassword_conTokenInvalido_retornarNotFound() throws Exception {
        // Armar el request con un token inexistente
        ResetPasswordRequest resetRequest = new ResetPasswordRequest("token-invalido-123", "nuevaPassword123");

        mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isNotFound());
    }
    
    // 7) Token expirado
    @Test
    void resetPassword_conTokenExpirado_retornarBadRequestExpirado() throws Exception {
        
        // 1. Registrar el usuario previamente (ya lo hiciste antes)
        String email = "usuPrueba@test.com";
        registrarUsuario("usuPrueba", "abc123456", email);
        
        // 2. Simular solicitud de recuperación de contraseña
        OlvidePasswordRequest request = new OlvidePasswordRequest(email);
        mockMvc.perform(post("/api/password/public/generartokenexpirado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // 3. Buscar el token generado en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        TokenResetPassword tokenReset = passwordResetTokenRepository
            .findByUsuarioOrderByFechaExpiracionDesc(usuario)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Token no generado"));

        // 4. Enviar contraseña inválida con el token correcto
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            tokenReset.getToken(), "contraseniaOk" // Contraseña valida
        );
        
        mockMvc.perform(post("/api/password/public/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(resetRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El token expiró."));
    }
    
    // 8) Token usado
    @Test
    void resetPassword_conTokenExpirado_retornarBadRequestUsado() throws Exception {
        
        // 1. Registrar el usuario previamente (ya lo hiciste antes)
        String email = "usuPrueba@test.com";
        registrarUsuario("usuPrueba", "abc123456", email);
        
        // 2. Simular solicitud de recuperación de contraseña
        OlvidePasswordRequest request = new OlvidePasswordRequest(email);
        mockMvc.perform(post("/api/password/public/generartokenusado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // 3. Buscar el token generado en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        TokenResetPassword tokenReset = passwordResetTokenRepository
            .findByUsuarioOrderByFechaExpiracionDesc(usuario)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Token no generado"));

        // 4. Enviar contraseña inválida con el token correcto
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            tokenReset.getToken(), "contraseniaOk" // Contraseña valida
        );
        
        mockMvc.perform(post("/api/password/public/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(resetRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("El token ya fue usado."));
    }
    
    // 9) Contraseña con validación fallida (demasiado corta, etc.)
    @Test
    void resetPassword_conContrasenaInvalida_retornarBadRequestMin() throws Exception {
        // 1. Registrar el usuario previamente (ya lo hiciste antes)
        String email = "usuPrueba@test.com";
        registrarUsuario("usuPrueba", "abc123456", email);
        
        // 2. Simular solicitud de recuperación de contraseña
        OlvidePasswordRequest request = new OlvidePasswordRequest(email);
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // 3. Buscar el token generado en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        TokenResetPassword tokenReset = passwordResetTokenRepository
            .findByUsuarioOrderByFechaExpiracionDesc(usuario)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Token no generado"));

        // 4. Enviar contraseña inválida con el token correcto
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            tokenReset.getToken(), "123" // Contraseña inválida (corta)
        );

        mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La contraseña debe tener entre 6 y 16 caracteres."));
    }
    
    // 10) Contraseña con validación fallida (demasiado larga, etc.)
    @Test
    void resetPassword_conContrasenaInvalida_retornarBadRequestMax() throws Exception {
        // 1. Registrar el usuario previamente (ya lo hiciste antes)
        String email = "usuPrueba@test.com";
        registrarUsuario("usuPrueba", "abc123456", email);
        
        // 2. Simular solicitud de recuperación de contraseña
        OlvidePasswordRequest request = new OlvidePasswordRequest(email);
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // 3. Buscar el token generado en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();

        TokenResetPassword tokenReset = passwordResetTokenRepository
            .findByUsuarioOrderByFechaExpiracionDesc(usuario)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Token no generado"));

        // 4. Enviar contraseña inválida con el token correcto
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(
            tokenReset.getToken(), "contraseniamuylarga1234" // Contraseña inválida (larga)
        );

        mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La contraseña debe tener entre 6 y 16 caracteres."));
    }
    
    // 11) Token válido pero para otro usuario
    @Test
    void resetPassword_conTokenValidoPeroParaOtroUsuario_retornarBadRequest() throws Exception {
        // Disparar solicitud de recuperación de contraseña para el primer usuario
        OlvidePasswordRequest olvidoRequest1 = new OlvidePasswordRequest("usuario@test.com");
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(olvidoRequest1)))
            .andExpect(status().isOk());

        // Disparar solicitud de recuperación de contraseña para el segundo usuario
        OlvidePasswordRequest olvidoRequest2 = new OlvidePasswordRequest("otroUsuario@test.com");
        mockMvc.perform(post("/api/password/public/olvide-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(olvidoRequest2)))
            .andExpect(status().isOk());

        // Obtener el token generado para el primer usuario
        Usuario usuario1 = usuarioRepository.findByEmail("usuario@test.com").orElseThrow();
        TokenResetPassword token1 = tokenResetPasswordRepository.findTopByUsuarioOrderByIdDesc(usuario1)
            .orElseThrow();

        // Armar el request para resetear la contraseña con el token del primer usuario
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(token1.getToken(), "nuevaPasswordSegura1");

        // Intentar resetear la contraseña con el token del primer usuario, pero para el segundo usuario
        mockMvc.perform(post("/api/password/public/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
            .andExpect(status().isBadRequest());  // El sistema debería responder con un Bad Request
    }
    
    //                                      /public/change-password
    // 12) Token válido, contraseña actual correcta
    @Test
    void resetPassword_conTokenValidoYContrasenaActualCorrecta_retornarOk() throws Exception {
        // Creamos la nueva contraseña
        String nuevaContraseña = "pruebacontra";

        // Intentamos hacer el cambio de contraseña con el token del usuario logueado (en este caso, tokenUsuario)
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("123456", nuevaContraseña); // "123456" es la contraseña actual

        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenUsuario) // Usamos el token válido
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andExpect(status().isOk()); // Esperamos una respuesta 200 OK si el cambio fue exitoso
    }
    
    // 13) Token válido, contraseña actual incorrecta
    @Test
    void resetPassword_conTokenValidoYContrasenaActualIncorrecta_retornarBadRequest() throws Exception {
        // La contraseña actual incorrecta
        String contraseñaIncorrecta = "123456789"; 
        String nuevaContraseña = "pruebacontra";

        // Creamos el objeto de solicitud con la contraseña actual incorrecta
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(contraseñaIncorrecta, nuevaContraseña);

        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenUsuario) // Usamos el token válido
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andExpect(status().isBadRequest()); // Esperamos una respuesta 400 Bad Request debido a la contraseña incorrecta
    }
    
    // 14) Token vencido o inválido (JWT)
    @Test
    void resetPassword_conTokenVencidoOInvalido_retornarUnauthorized() throws Exception {
        // Crear un token vencido o inválido (puedes crear un token inválido de forma manual o simular uno)
        String tokenInvalido = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMTQ4NCIsInVzZXJuYW1lIjoi"
                + "dXN1YXJpbyIsImp0aSI6IjBjMjg5OGRhLTM2MWItNGYyMi04OGI5LWNkODEzZGI2ZDA5NC"
                + "IsImlhdCI6MTc0NjQ0ODU5MiwiZXhwIjoxNzQ2NDUwMzkyfQ.pCdw0TUP8Jrw8O7SNIF48"
                + "V0_Ee2rRiZmoF4V_WwaH5_hz5_sRdtijbc_xj9gb_bhKUL260UY-PgWDd0CZfBFEZKSIt0"
                + "t-MoTb43tzr3pBK8SqY-OmjfM9zcu7oLTupQ7CECE7yBjo5olSDd_WuU6SE8EljRhl9QCI"
                + "jnj8Yoz21MeRDabeAmtMXuhclBKZIjbu2y_n70JuFN1451ZngPy1GAdcEx8qlfUxlpjw6_"
                + "cCWyBOqeuHfw-7n2zjFnL9RWBYDjvAfnKuZtvIO1Ny1VCBcJyMBDcVIIqXTKu9M1L1Jj2k"
                + "vu9XUv_Yvqi2N6oE07Byks4tUVRhPQ077eIDI5d1Q\",\"refreshToken\":\"4b667ae"
                + "2-5b6c-46bb-b5cd-5e41c658eee4";  // Un token inválido o ya expirado

        // Creamos el objeto de solicitud con la contraseña actual incorrecta
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("contraseñaActual123", "nuevaContraseñaSegura1");

        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenInvalido) // Usamos el token inválido o expirado
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andExpect(status().isUnauthorized()); // Esperamos una respuesta 401 Unauthorized
    }
    
    // 15) Sin token
    @Test
    void resetPassword_sinToken_retornarUnauthorized() throws Exception {
        // Crear la solicitud para cambiar la contraseña (sin enviar el token)
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "nuevaContraseñaSegura1");

        // Realizar la solicitud al endpoint /change-password sin incluir el token en los encabezados
        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest))) // Sin token en los headers
            .andExpect(status().isUnauthorized()); // Esperamos un 401 Unauthorized
    }
    
    // 16) Contraseña nueva inválida (demasiado corta)
    @Test
    void resetPassword_contraseñaNuevaInvalida_retornarBadRequestMin() throws Exception {
        // Crear la solicitud de cambio de contraseña con una contraseña que no cumple con los requisitos
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("123456", "123"); // Contraseña demasiado corta

        // Realizar la solicitud al endpoint /change-password con la contraseña inválida
        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .header("Authorization", "Bearer " + tokenUsuario)) // Incluir un token válido
            .andExpect(status().isBadRequest()) // Esperamos un 400 BadRequest
            .andExpect(jsonPath("$.message").value("La nueva contraseña debe tener entre 6 y 16 caracteres")); // Mensaje de error esperado
    }
    
    // 17) Contraseña nueva inválida (demasiado larga)
    @Test
    void resetPassword_contraseñaNuevaInvalida_retornarBadRequestMax() throws Exception {
        // Crear la solicitud de cambio de contraseña con una contraseña que no cumple con los requisitos
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("123456", "contraseñarmuylarga"); // Contraseña demasiado corta

        // Realizar la solicitud al endpoint /change-password con la contraseña inválida
        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .header("Authorization", "Bearer " + tokenUsuario)) // Incluir un token válido
            .andExpect(status().isBadRequest()) // Esperamos un 400 BadRequest
            .andExpect(jsonPath("$.message").value("La nueva contraseña debe tener entre 6 y 16 caracteres")); // Mensaje de error esperado
    }
    
    // 18) Cambiar contraseña con mismo valor que actual (opcional según reglas)
    @Test
    void resetPassword_conMismaContraseña_retornarBadRequest() throws Exception {
        // Supongamos que la contraseña actual es "123456", y tratamos de cambiarla a la misma contraseña
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("123456", "123456"); // La misma contraseña

        // Realizar la solicitud al endpoint /change-password con la misma contraseña
        mockMvc.perform(post("/api/password/private/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest))
                .header("Authorization", "Bearer " + tokenUsuario)) // Incluir un token válido
            .andExpect(status().isBadRequest()) // Esperamos un 400 BadRequest
            .andExpect(jsonPath("$.error").value("Contraseña repetida"))
            .andExpect(jsonPath("$.message").value("La nueva contraseña no puede ser igual a la actual."));
    }
    
}
