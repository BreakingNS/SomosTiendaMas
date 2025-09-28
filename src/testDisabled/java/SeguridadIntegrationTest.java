
package com.breakingns.SomosTiendaMas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.entidades.usuario.controller.UsuarioController;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioService;
import com.breakingns.SomosTiendaMas.model.auth.RolNombre;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@Sql(statements = {
    "DELETE FROM carrito",
    "DELETE FROM usuario_roles",
    "DELETE FROM usuario",
    //"ALTER SEQUENCE usuario_id_usuario_seq RESTART WITH 1",
    //"ALTER SEQUENCE rol_id_rol_seq RESTART WITH 1",
    //"ALTER SEQUENCE carrito_id_carrito_seq RESTART WITH 1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@RequiredArgsConstructor
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeguridadIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UsuarioController usuControl;
    private final UsuarioService usuServ;
    private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final RolService rolServ;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    private String tokenAdmin;
    private String tokenUsuario;
    private Long idAdmin;
    private Long idUsuario;
    
    @BeforeAll
    void setUpBase() {
        // Limpieza
        usuarioRepository.deleteAll();

        //rolRepository.deleteAll();
        /*
        // Crear roles
        Rol rolAdmin = rolRepository.save(new Rol(RolNombre.ROLE_ADMIN));
        Rol rolUser = rolRepository.save(new Rol(RolNombre.ROLE_USUARIO));
        */
        // Guarda la contraseña original antes de codificar
        String rawPasswordAdmin = "admin123";
        String rawPasswordUser = "user123";
        
        // Crear usuario admin
        Usuario admin = new Usuario();
        admin.setUsername("adminExample");
        admin.setPassword(rawPasswordAdmin);
        admin.setEmail("correoprueba@noenviar.com");
        usuControl.registerAdmin(admin);

        // Crear usuario normal
        Usuario user = new Usuario();
        user.setUsername("usuarioExample");
        user.setPassword(rawPasswordUser);
        user.setEmail("correoprueba@noenviar.com");
        usuControl.registerUser(user);
        
        // Creo LoginRequest
        LoginRequest loginAdmin = new LoginRequest();
        loginAdmin.setUsername(admin.getUsername());
        loginAdmin.setPassword(rawPasswordAdmin);
        LoginRequest loginUser = new LoginRequest();
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(rawPasswordUser);
        
        // Autenticacion de credenciales y creacion del objeto Authentication
        Authentication authenticationAdmin = authenticationManager.authenticate( //Autentica Admin
                new UsernamePasswordAuthenticationToken(
                        loginAdmin.getUsername(),
                        loginAdmin.getPassword()
                )
        );
        
        Authentication authenticationUser = authenticationManager.authenticate( //Autentica Usuario
                new UsernamePasswordAuthenticationToken(
                        loginUser.getUsername(),
                        loginUser.getPassword()
                )
        );
        
        // Generar tokens
        tokenAdmin = jwtTokenProvider.generarToken(authenticationAdmin);
        tokenUsuario = jwtTokenProvider.generarToken(authenticationUser);
        
        /*
        // Id de cada usuario
        Long idAdmin = admin.getId_usuario();
        Long idUsuario = user.getId_usuario();
        */
        
        idAdmin = jwtTokenProvider.obtenerIdDelToken(tokenAdmin);
        idUsuario = jwtTokenProvider.obtenerIdDelToken(tokenUsuario);
    }
    
    //Test de acceso exitoso a una ruta protegida (con token válido)
    @Test
    void accesoRutaProtegidaConTokenUsuario() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
            .header("Authorization", "Bearer " + tokenUsuario))
            .andExpect(status().isOk());
    }
    
    //Test de acceso fallido a una ruta protegida (sin token)
    @Test
    void accesoRutaProtegidaSinToken() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)) // misma ruta protegida
                .andExpect(status().isUnauthorized()); // 401 esperado
    }

        //Test de endpoint con @PreAuthorize
    
    //Acceso con token de admin
    @Test
    void accesoSoloAdminConTokenAdminOk() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
    
    @Test
    void accesoSoloAdminConTokenAdminNoOk() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idAdmin)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
    }
    
    //Acceso con token de usuario con Usuario
    @Test
    void accesoSoloAdminConTokenUsuarioOk() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk()); // 200 esperado
    }
    
    //Acceso con token de usuario con Admin
    @Test
    void accesoSoloAdminConTokenUsuarioOk2() throws Exception {
        mockMvc.perform(get("/api/carrito/traer/" + idUsuario)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk()); // 403 esperado
    }

    @Test
    public void testGenerateToken() throws Exception {

        System.out.println("Generated Token: " + tokenAdmin);
        System.out.println("Generated Token: " + tokenUsuario);

        // Asegúrate de que el token no esté vacío
        assertNotNull(tokenAdmin);
        assertTrue(tokenAdmin.length() > 0);
        assertNotNull(tokenUsuario);
        assertTrue(tokenUsuario.length() > 0);
    }
}
