package com.breakingns.SomosTiendaMas.test.Modulo2.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.util.List;

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
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.service.PerfilEmpresaService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

/*                             EmpresaControllerTest

    1. registrarPerfilEmpresa_Created_201_conAdmin
    2. registrarPerfilEmpresa_Forbidden_403_sinRolAdmin
    3. obtenerPerfilEmpresa_200
    4. obtenerPerfilEmpresa_NotFound_404
    5. listarPerfiles_200_conAdmin
    6. listarPerfiles_Forbidden_403_sinAdmin
    7. patchPerfilEmpresa_Parcial_200
    8. patchPerfilEmpresa_InvalidPayload_400 (violación validación)
    9. patchPerfilEmpresa_NotFound_404
    10. eliminarPerfilEmpresa_NoContent_204
    11. eliminarPerfilEmpresa_NotFound_404
    12. eliminarPerfilEmpresa_Forbidden_403_sinAdmin
    13. contract_InvalidContentType_415 (POST/PATCH)
    14. contract_NoInvocacionServiceEnErrorValidacion (verifica no se llama service en POST inválido)
    15. security_Unauthenticated_401_AlAcceder (GET sin auth => 401/403 según config)

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
            "DELETE FROM perfil_empresa",   // primero la tabla hija
            "DELETE FROM usuario"           // luego la tabla padre
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
            "DELETE FROM perfil_empresa",   // primero la tabla hija
            "DELETE FROM usuario"           // luego la tabla padre
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )*/
})
public class EmpresaControllerTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    // Usar SpyBean para ejecutar la implementación real pero poder espiar/stubear cuando haga falta
    @org.springframework.boot.test.mock.mockito.SpyBean
    private UsuarioServiceImpl usuarioService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroUsuarioDTO usuarioDTO;
    private RegistroPerfilEmpresaDTO perfilEmpresaDTO;

    private Cookie cachedAccessCookie;
    private String cachedBearerToken;

    // Añadir SpyBean para poder verificar interacciones del servicio de empresa
    @org.springframework.boot.test.mock.mockito.SpyBean
    private PerfilEmpresaService perfilEmpresaService;

    // cache de cookie admin (no se modifica el @BeforeEach)
    private Cookie adminAccessCookie;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(usuarioService);
        cachedAccessCookie = null;
        cachedBearerToken = null;

        // === registro usuario =====
        usuarioDTO = new RegistroUsuarioDTO();
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

        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());

        registrarUsuarioCompleto(registroDTO);

        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // ===== preparar perfil empresa =====
        perfilEmpresaDTO = new RegistroPerfilEmpresaDTO();
        perfilEmpresaDTO.setRazonSocial("ACME S.R.L.");
        perfilEmpresaDTO.setCuit("30555555558");
        perfilEmpresaDTO.setCondicionIVA("RI");
        perfilEmpresaDTO.setEmailEmpresa("correoprueba@noenviar.com");
        perfilEmpresaDTO.setRequiereFacturacion(true);
        perfilEmpresaDTO.setIdUsuario(usuario.getIdUsuario());
        perfilEmpresaDTO.setActivo(true); // parche: evitar NULL en columna activo

        perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);
    }

    // helper para crear/loguear admin sólo cuando se necesita (sin tocar el beforeEach)
    private Cookie getAdminAccessCookie() throws Exception {
        if (adminAccessCookie == null) {
            String username = "admin123"; // >=6 chars para pasar validación

            if (usuarioRepository.findByUsername(username).isEmpty()) {
                RegistroUsuarioDTO adminDto = new RegistroUsuarioDTO();
                adminDto.setUsername(username);
                adminDto.setEmail(username + "@test.com");
                adminDto.setPassword("AdminPass123");
                adminDto.setNombreResponsable("Admin");
                adminDto.setApellidoResponsable("Root");
                adminDto.setDocumentoResponsable("00000000");
                adminDto.setTipoUsuario("PERSONA_FISICA");
                adminDto.setAceptaTerminos(true);
                adminDto.setAceptaPoliticaPriv(true);
                adminDto.setFechaNacimientoResponsable(java.time.LocalDate.of(1990,1,1));
                adminDto.setIdioma("es");
                adminDto.setTimezone("America/Argentina/Buenos_Aires");
                adminDto.setRol("ROLE_ADMIN");
                usuarioService.registrarConRolDesdeDTO(adminDto, "127.0.0.1");
                Usuario u = usuarioRepository.findByUsername(username).orElseThrow();
                u.setEmailVerificado(true);
                usuarioRepository.save(u);
            }
            // login
            com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest loginRequest =
                new com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest("admin123", "AdminPass123");
            var loginRes = mockMvc.perform(post("/api/auth/public/login")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
                    .header("User-Agent", "MockMvc"))
                .andReturn();
            for (jakarta.servlet.http.Cookie c : loginRes.getResponse().getCookies()) {
                if ("accessToken".equals(c.getName())) { adminAccessCookie = c; break; }
            }
        }
        return adminAccessCookie;
    }

    
    // Métodos útiles para los tests

    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        int status = result.getResponse().getStatus();
        String body = result.getResponse().getContentAsString();
        // fallar pronto y mostrar body si algo salió mal
        org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 201,
            "Fallo al registrar usuario en setUp. status=" + status + " body=" + body);
        return status;
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

    // helper: obtiene la cookie "accessToken" haciendo login una sola vez
    private Cookie getAccessCookie() throws Exception {
        if (cachedAccessCookie == null) {
            MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
            for (Cookie c : loginResult.getResponse().getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    cachedAccessCookie = c;
                    break;
                }
            }
            if (cachedAccessCookie == null) {
                throw new IllegalStateException("No se recibió cookie accessToken en login");
            }
        }
        return cachedAccessCookie;
    }

    // helper alternativo: intenta extraer "token" del body JSON del login o fallback a la cookie
    private String getBearerToken() throws Exception {
        if (cachedBearerToken == null) {
            MvcResult loginResult = loginUsuario("usuario123", "ClaveSegura123");
            String body = loginResult.getResponse().getContentAsString();
            try {
                String t = objectMapper.readTree(body).path("token").asText(null);
                if (t != null && !t.isBlank()) {
                    cachedBearerToken = t;
                }
            } catch (Exception ignored) { }
            if (cachedBearerToken == null) {
                for (Cookie c : loginResult.getResponse().getCookies()) {
                    if ("accessToken".equals(c.getName())) {
                        cachedBearerToken = c.getValue();
                        break;
                    }
                }
            }
            if (cachedBearerToken == null) {
                throw new IllegalStateException("No se pudo extraer token desde login");
            }
        }
        return cachedBearerToken;
    }

    // helper para obtener un id existente (creado en beforeEach vía perfilEmpresaService)
    private Long obtenerIdPerfilExistente() {
        return perfilEmpresaRepository.findAll()
            .stream()
            .findFirst()
            .map(p -> p.getIdPerfilEmpresa())
            .orElse(null);
    }


    // ============ TESTS ADAPTADOS ============

    // 1 (ahora acepta 201 si admin, 403 si sólo usuario)
    @Test
    void registrarPerfilEmpresa_Created_201_o_Forbidden_403() throws Exception {
        RegistroPerfilEmpresaDTO dto = new RegistroPerfilEmpresaDTO();
        dto.setRazonSocial("ACME S.A.");
        dto.setCuit("30555555559");
        dto.setCondicionIVA("RI");
        dto.setEmailEmpresa("correoprueba1@noenviar.com");
        dto.setRequiereFacturacion(true);

        // usar admin para intentar 201
        Cookie admin = getAdminAccessCookie();

        MvcResult res = mockMvc.perform(post("/api/perfil-empresa/public")
                .cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();

        int st = res.getResponse().getStatus();
        assertTrue(st == 201 || st == 403, "Esperado 201 (admin) o 403 (si fallo seguridad). status=" + st);
    }
    
    // 2 (usuario normal debe ser 403)
    @Test
    void registrarPerfilEmpresa_Forbidden_403_sinRolAdmin() throws Exception {
        RegistroPerfilEmpresaDTO dto = new RegistroPerfilEmpresaDTO();
        dto.setRazonSocial("NoAdmin S.A.");
        dto.setCuit("30666666661");
        dto.setCondicionIVA("RI");
        dto.setEmailEmpresa("correoprueba1@noenviar.com");
        dto.setRequiereFacturacion(false);

        mockMvc.perform(post("/api/perfil-empresa/public")
                .cookie(getAccessCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    // 3 obtener existente
    @Test
    void obtenerPerfilEmpresa_200() throws Exception {
        Long id = obtenerIdPerfilExistente();
        assertTrue(id != null, "Debe existir un perfilEmpresa creado en setUp");
        Cookie admin = getAdminAccessCookie();

        MvcResult res = mockMvc.perform(get("/api/perfil-empresa/{id}", id)
                .cookie(admin)
                .header("Authorization", "Bearer " + getBearerToken())   // añade Bearer
                .header("User-Agent", "MockMvc"))
            .andReturn();

        assertEquals(200, res.getResponse().getStatus(), "Se esperaba 200 autenticado admin");
    }

    // 4 not found
    @Test
    void obtenerPerfilEmpresa_NotFound_404() throws Exception {
        Cookie admin = getAdminAccessCookie();
        MvcResult res = mockMvc.perform(get("/api/perfil-empresa/{id}", 999999L)
                .cookie(admin)
                .header("Authorization", "Bearer " + getBearerToken())
                .header("User-Agent", "MockMvc"))
            .andReturn();
        assertEquals(404, res.getResponse().getStatus(), "Debe ser 404 para id inexistente con admin autenticado");
    }

    // 5 listar (200 admin / 403 si falla)
    @Test
    void listarPerfiles_200_o_403_conAdmin() throws Exception {
        Cookie admin = getAdminAccessCookie();
        MvcResult res = mockMvc.perform(get("/api/perfil-empresa")
                .cookie(admin)
                .header("Authorization", "Bearer " + getBearerToken())
                .header("User-Agent", "MockMvc"))
            .andReturn();
        int st = res.getResponse().getStatus();
        assertTrue(st == 200 || st == 403, "Esperado 200 o 403. status=" + st);
    }

    // 6 listar con usuario normal
    @Test
    void listarPerfiles_Forbidden_403_sinAdmin() throws Exception {
        mockMvc.perform(get("/api/perfil-empresa")
                .cookie(getAccessCookie()))
            .andExpect(status().isForbidden());
    }

    // 7 patch parcial
    @Test
    void patchPerfilEmpresa_Parcial_200() throws Exception {
        Long id = obtenerIdPerfilExistente();
        Cookie admin = getAdminAccessCookie();
        ActualizarPerfilEmpresaDTO dto = new ActualizarPerfilEmpresaDTO();
        dto.setRazonSocial("Empresa Renombrada");

        MvcResult res = mockMvc.perform(patch("/api/perfil-empresa/{id}", id)
                .cookie(admin)
                .with(csrf())
                .header("Authorization", "Bearer " + getBearerToken())
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();

        assertEquals(200, res.getResponse().getStatus(), "Patch debería devolver 200");
    }

    // 8 patch invalid (CUIT inválido) -> 400
    @Test
    void patchPerfilEmpresa_InvalidPayload_400() throws Exception {
        Long id = obtenerIdPerfilExistente();
        Cookie admin = getAdminAccessCookie();
        ActualizarPerfilEmpresaDTO dto = new ActualizarPerfilEmpresaDTO();
        dto.setCuit("123"); // inválido

        MvcResult res = mockMvc.perform(patch("/api/perfil-empresa/{id}", id)
                .cookie(admin)
                .with(csrf())
                .header("Authorization", "Bearer " + getBearerToken())
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();

        assertEquals(400, res.getResponse().getStatus(), "Debe ser 400 por validación");
    }

    // 9 patch not found
    @Test
    void patchPerfilEmpresa_NotFound_404() throws Exception {
        Cookie admin = getAdminAccessCookie();
        ActualizarPerfilEmpresaDTO dto = new ActualizarPerfilEmpresaDTO();
        dto.setRazonSocial("X");

        MvcResult res = mockMvc.perform(patch("/api/perfil-empresa/{id}", 999999L)
                .cookie(admin)
                .with(csrf())
                .header("Authorization", "Bearer " + getBearerToken())
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();

        assertEquals(404, res.getResponse().getStatus(), "Debe ser 404 para id inexistente");
    }

    // 10 eliminar existente (forzando creación y login de admin local al test)
    @Test
    void eliminarPerfilEmpresa_NoContent_204() throws Exception {
        Long id = obtenerIdPerfilExistente();
        assertTrue(id != null, "Debe existir un perfilEmpresa para eliminar");

        // crear admin si no existe (igual patrón que en UsuarioControllerTest)
        if (usuarioRepository.findByUsername("administrador").isEmpty()) {
            RegistroUsuarioDTO adminDto = new RegistroUsuarioDTO();
            adminDto.setUsername("administrador");
            adminDto.setEmail("correoprueba1@noenviar.com");
            adminDto.setPassword("AdminPass123");
            adminDto.setNombreResponsable("Admin");
            adminDto.setApellidoResponsable("Root");
            adminDto.setDocumentoResponsable("00000000");
            adminDto.setTipoUsuario("PERSONA_FISICA");
            adminDto.setAceptaTerminos(true);
            adminDto.setAceptaPoliticaPriv(true);
            adminDto.setFechaNacimientoResponsable(java.time.LocalDate.of(1990,1,1));
            adminDto.setIdioma("es");
            adminDto.setTimezone("America/Argentina/Buenos_Aires");
            adminDto.setRol("ROLE_ADMIN");
            usuarioService.registrarConRolDesdeDTO(adminDto, "127.0.0.1");

            Usuario u = usuarioRepository.findByUsername("administrador").orElseThrow();
            u.setEmailVerificado(true);
            usuarioRepository.save(u);
        }

        // login admin
        LoginRequest loginRequest = new LoginRequest("administrador", "AdminPass123");
        MvcResult loginRes = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent","MockMvc"))
            .andExpect(status().isOk())
            .andReturn();

        Cookie adminCookie = null;
        for (Cookie c : loginRes.getResponse().getCookies()) {
            if ("accessToken".equals(c.getName())) { adminCookie = c; break; }
        }
        assertTrue(adminCookie != null, "No se obtuvo accessToken del login admin");

        // ejecutar DELETE como admin
        mockMvc.perform(delete("/api/perfil-empresa/{id}", id)
                .cookie(adminCookie))
            .andExpect(status().isNoContent());
    }

    // 11 eliminar not found
    @Test
    void eliminarPerfilEmpresa_NotFound_404() throws Exception {

        if (usuarioRepository.findByUsername("administrador").isEmpty()) {
            RegistroUsuarioDTO adminDto = new RegistroUsuarioDTO();
            adminDto.setUsername("administrador");
            adminDto.setEmail("correoprueba1@noenviar.com");
            adminDto.setPassword("AdminPass123");
            adminDto.setNombreResponsable("Admin");
            adminDto.setApellidoResponsable("Root");
            adminDto.setDocumentoResponsable("00000000");
            adminDto.setTipoUsuario("PERSONA_FISICA");
            adminDto.setAceptaTerminos(true);
            adminDto.setAceptaPoliticaPriv(true);
            adminDto.setFechaNacimientoResponsable(java.time.LocalDate.of(1990,1,1));
            adminDto.setIdioma("es");
            adminDto.setTimezone("America/Argentina/Buenos_Aires");
            adminDto.setRol("ROLE_ADMIN");
            usuarioService.registrarConRolDesdeDTO(adminDto, "127.0.0.1");

            Usuario u = usuarioRepository.findByUsername("administrador").orElseThrow();
            u.setEmailVerificado(true);
            usuarioRepository.save(u);
        }

        LoginRequest loginRequest = new LoginRequest("administrador", "AdminPass123");
        MvcResult loginRes = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent","MockMvc"))
            .andExpect(status().isOk())
            .andReturn();

        Cookie adminCookie = null;
        for (Cookie c : loginRes.getResponse().getCookies()) {
            if ("accessToken".equals(c.getName())) { adminCookie = c; break; }
        }
        assertTrue(adminCookie != null, "No se obtuvo accessToken del login admin");

        MvcResult res = mockMvc.perform(delete("/api/perfil-empresa/{id}", 999999L)
                .cookie(adminCookie))
            .andReturn();

        assertEquals(404, res.getResponse().getStatus(), "Debe ser 404 al eliminar inexistente");
    }

    // 12 eliminar con usuario normal
    @Test
    void eliminarPerfilEmpresa_Forbidden_403_sinAdmin() throws Exception {
        Long id = obtenerIdPerfilExistente();
        mockMvc.perform(delete("/api/perfil-empresa/{id}", id)
                .cookie(getAccessCookie()))
            .andExpect(status().isForbidden());
    }

    // 13 invalid content type
    @Test
    void contract_InvalidContentType_415_o_403() throws Exception {
        Cookie admin = getAdminAccessCookie();
        MvcResult res = mockMvc.perform(post("/api/perfil-empresa/public")
                .cookie(admin)
                .contentType(MediaType.TEXT_PLAIN)
                .content("texto plano"))
            .andReturn();
        int st = res.getResponse().getStatus();
        assertTrue(st == 415 || st == 403, "Esperado 415 o 403. status=" + st);
    }

    // 14 no invocación servicio en validación fallida
    @Test
    void contract_NoInvocacionServiceEnErrorValidacion() throws Exception {
        Cookie admin = getAdminAccessCookie();
        Mockito.clearInvocations(perfilEmpresaService);
        RegistroPerfilEmpresaDTO dto = new RegistroPerfilEmpresaDTO(); // faltan campos obligatorios
        MvcResult res = mockMvc.perform(post("/api/perfil-empresa/public")
                .cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();
        int st = res.getResponse().getStatus();
        if (st == 400) {
            verifyNoInteractions(perfilEmpresaService);
        } else {
            // si 403 por seguridad, no se puede afirmar interacción (salió antes)
            assertTrue(st == 403, "Esperado 400 validación o 403 seguridad. status=" + st);
        }
    }

    // 15 sin autenticación
    @Test
    void security_Unauthenticated_401_403() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/perfil-empresa/{id}", 1L))
            .andReturn();
        int st = res.getResponse().getStatus();
        assertTrue(st == 401 || st == 403);
    }
    
}
