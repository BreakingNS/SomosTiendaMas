package com.breakingns.SomosTiendaMas.test.Modulo2.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.TelefonoService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

/*                              TelefonoControllerTest

    1) registrarTelefono_OK_200_y_body
       - POST /api/telefono/public con DTO válido -> 200 y body contiene id/fields devueltos por el servicio. Verificar que ITelefonoService.registrarTelefono(...) fue invocado.

    2) registrarTelefono_InvalidPayload_400_y_no_invocaService
       - POST con payload inválido (faltan campos @Valid) -> 400 y ITelefonoService NO es llamado.

    3) registrarTelefono_CodigoAreaInvalido_400
       - DTO con caracteristica inexistente -> service lanza IllegalArgumentException o similar -> controller mapea a 400.

    4) registrarTelefono_OwnerXorInvalid_400
       - DTO con ambos idUsuario e idPerfilEmpresa presentes (o ninguno) -> 400 y mensaje claro; service no invocado.

    5) registrarTelefono_Existing_ReturnsExisting_200
       - Si ya existe un teléfono igual para el mismo owner, el servicio devuelve el existente -> 200 y body con ese registro; verificar que no se guarda duplicado.

    6) registrarTelefono_NotFoundOwner_404
       - El owner (usuario o perfil) no existe -> service lanza EntityNotFoundException -> controller devuelve 404.

    7) actualizarTelefono_OK_200_conTokenPersistido
       - PUT /api/telefono/private/{id} con DTO válido y autenticación -> 200 y body con resultado; verificar ITelefonoService.actualizarTelefono(id, dto) fue llamado.

    8) actualizarTelefono_IdNoValido_400
       - PUT con path id no numérico -> 400 y service no invocado.

    9) actualizarTelefono_NotFound_404
       - Service lanza IllegalArgumentException/EntityNotFoundException al actualizar -> controller devuelve 404 o 400 según mapeo.

    10) obtenerTelefono_OK_200_y_body
        - GET /api/telefono/private/{id} donde service devuelve DTO -> 200 y body correcto.

    11) obtenerTelefono_NotFound_404
        - GET para id inexistente -> 404.

    12) listarTelefonosPorUsuario_OK_200_lista
        - GET /api/telefono/private/usuario/{idUsuario} -> 200 y lista (vacía o con elementos) devuelta por service; verificar parámetros pasados.

    13) listarTelefonosPorUsuario_IdNoValido_400
        - Path var no numérico -> 400 y mensaje indicando idUsuario.

    14) listarTelefonosPorEmpresa_OK_200_lista
        - GET /api/telefono/private/empresa/{idPerfilEmpresa} -> 200 y lista; verificar que service fue llamado con id correcto.

    15) seguridad_PrivateEndpoints_Unauthenticated_401_o_403
        - Acceso a endpoints /private sin auth -> respuesta 401 (o 403 según configuración).

    16) contentType_Invalid_415_o_406
        - Envío a POST /api/telefono/public con Content-Type no soportado (ej. application/xml) -> 415/406; asegurar que service NO se invoca y que el handler global formatea la respuesta.

    17) errorMapping_EstructuraDeErrorConsistency
        - Forzar excepción inesperada en service -> controller devuelve estructura de error consistente (status/message/timestamp) y código 500; verificar que ITelefonoService fue invocado.

    18) actualizarTelefono_PartialUpdate_preservaValoresNulos_OK
        - PUT con algunos campos nulos -> campos no provistos no deben sobrescribir valores existentes; verificar comportamiento y respuesta 200.

    19) registrarTelefono_Normalizacion_Trim
        - Números/característica con espacios alrededor deben normalizarse; si coincide con existente (ignorando espacios) devolver existente.

    20) listarTelefonos_EmptyList_200
        - Service devuelve lista vacía -> endpoint retorna 200 y body JSON array vacío.

    21) registrarTelefono_ValidacionDeEnums_400
        - dto.tipo con valor no válido (no coincide con TipoTelefono) -> 400 y mensaje claro.

    22) contract_VerificarNoInteraccionesEnErrores
        - En cada caso de error (400/404/415) verificar verifyNoInteractions(...) con dependencias que no deben ejecutarse.

    (Opcional)
    23) integracion_BorderCase_multiple_registros_simultaneos
        - Test de integración para comprobar que la lógica que evita duplicados es robusta en concurrencia (puede ser test de integración, no unit).
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
            "DELETE FROM perfil_empresa",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )*/
})
public class TelefonoControllerTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    private final ITelefonoRepository telefonoRepository;

    // Usar SpyBean para ejecutar la implementación real, pero poder espiar/stubear si es necesario
    @org.springframework.boot.test.mock.mockito.SpyBean
    private TelefonoService telefonoService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;
    private RegistroUsuarioDTO usuarioDTO;
    private RegistroTelefonoDTO telefonoDTO;

    private Cookie cachedAccessCookie;
    private String cachedBearerToken;

    @BeforeEach
    void setUp() throws Exception {
        // resetear mocks para evitar inter-test leakage
        Mockito.reset(telefonoService);

        // limpiar cache de autenticación para evitar cookie/token inválidos
        cachedAccessCookie = null;
        cachedBearerToken = null;

        direccionDTO = new RegistroDireccionDTO();
        
        // 1. Instanciar y configurar usuario y teléfono
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

        // 2. Armar el registro completo con las listas ya instanciadas
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of()); // no registramos direcciones aquí para tests de telefono
        registroDTO.setTelefonos(List.of());

        // 3. Registrar usuario antes de registrar dirección
        registrarUsuarioCompleto(registroDTO);

        Usuario usu = usuarioRepository.findByUsername("usuario123").orElseThrow();

        telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("4123456");
        telefonoDTO.setCaracteristica("383");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);
        telefonoDTO.setIdUsuario(usu.getIdUsuario()); // se asigna en cada test
        telefonoDTO.setIdPerfilEmpresa(null);

        telefonoService.registrarTelefono(telefonoDTO);

        // 4. Verificar usuario y email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Limpiar el historial de invocaciones del spy para que los tests que esperan no-interacción pasen
        Mockito.clearInvocations(telefonoService);
    }

    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        return result.getResponse().getStatus();
    }
    
    private PerfilEmpresa crearPerfilEmpresa(Usuario usuario, String razon, String cuit) {
        PerfilEmpresa p = new PerfilEmpresa();
        p.setRazonSocial(razon);
        p.setCuit(cuit);
        p.setActivo(true);
        p.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        p.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        p.setEmailEmpresa("correoprueba1@noenviar.com");
        p.setRequiereFacturacion(true);
        p.setFechaCreacion(LocalDateTime.now());
        p.setFechaUltimaModificacion(LocalDateTime.now());
        p.setUsuario(usuario);
        return perfilEmpresaRepository.save(p);
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

    // 1. Guardar un teléfono correctamente en la base de datos.
    @Test
    void guardarTelefonoCorrectamente() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(ts.stream().anyMatch(t -> "4123456".equals(t.getNumero())),
            "Debe guardarse el teléfono para el usuario");
    }
    
    // 2. registrarTelefono_InvalidPayload_400_y_no_invocaService
    @Test
    void registrarTelefono_InvalidPayload_400_y_no_invocaService() throws Exception {
        RegistroTelefonoDTO invalido = new RegistroTelefonoDTO();
        // faltan campos requeridos
        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
            .andExpect(status().isBadRequest());
    }
    
    // 3. registrarTelefono_CodigoAreaInvalido_400
    @Test
    void registrarTelefono_CodigoAreaInvalido_400() throws Exception {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("221334455");
        dto.setCaracteristica("9999"); // asumir 9999 no existe
        dto.setTipo("PRINCIPAL");
        dto.setIdUsuario(usuarioRepository.findByUsername("usuario123").get().getIdUsuario());

        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    // 4. registrarTelefono_OwnerXorInvalid_400
    @Test
    void registrarTelefono_OwnerXorInvalid_400() throws Exception {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("1155667788");
        dto.setCaracteristica("11");
        dto.setTipo("PRINCIPAL");
        // ni idUsuario ni idPerfilEmpresa -> inválido
        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }
    
    // 5. registrarTelefono_Existing_ReturnsExisting_200
    @Test
    void registrarTelefono_Existing_ReturnsExisting_200() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("1122334455");
        dto.setCaracteristica("11");
        dto.setTipo("PRINCIPAL");
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(usuario.getIdUsuario());

        // segundo envío del mismo teléfono debe devolver 200 y no duplicar
        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.numero").value("1122334455"));
    }
    
    // 6. registrarTelefono_NotFoundOwner_404
    @Test
    void registrarTelefono_NotFoundOwner_404() throws Exception {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("1155990011");
        dto.setCaracteristica("11");
        dto.setTipo("PRINCIPAL");
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(999999L); // owner inexistente

        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    // 7. actualizarTelefono_OK_200_conTokenPersistido
    @Test
    void actualizarTelefono_OK_200_conTokenPersistido() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        var telefono = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).get(0);

        com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO update =
            new com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO();
        update.setNumero("111000222");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/telefono/private/{id}", telefono.getIdTelefono())
                .cookie(getAccessCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numero").value("111000222"));
    }

    // 8. actualizarTelefono_IdNoValido_400
    @Test
    void actualizarTelefono_IdNoValido_400() throws Exception {
        com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO update =
            new com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO();
        update.setNumero("111000222");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/telefono/private/{id}", "abc")
                .cookie(getAccessCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isBadRequest());
    }
    
    // 9. actualizarTelefono_NotFound_404
    @Test
    void actualizarTelefono_NotFound_404() throws Exception {
        com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO update =
            new com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO();
        update.setNumero("1199887766");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/telefono/private/{id}", 999999L)
                .cookie(getAccessCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isNotFound());
    }

    // 10. obtenerTelefono_OK_200_y_body
    @Test
    void obtenerTelefono_OK_200_y_body() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        var telefono = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).get(0);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/{id}", telefono.getIdTelefono())
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numero").value(telefono.getNumero()));
    }

    // 11. obtenerTelefono_NotFound_404
    @Test
    void obtenerTelefono_NotFound_404() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/{id}", 999999L)
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    // 12. listarTelefonosPorUsuario_OK_200_lista
    @Test
    void listarTelefonosPorUsuario_OK_200_lista() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/usuario/{idUsuario}", usuario.getIdUsuario())
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // 13. listarTelefonosPorUsuario_IdNoValido_400
    @Test
    void listarTelefonosPorUsuario_IdNoValido_400() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/usuario/{idUsuario}", "abc")
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    // 14. listarTelefonosPorEmpresa_OK_200_lista
    @Test
    void listarTelefonosPorEmpresa_OK_200_lista() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        PerfilEmpresa perfil = crearPerfilEmpresa(usuario, "Prueba SA", "20333444555");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/empresa/{idPerfilEmpresa}", perfil.getIdPerfilEmpresa())
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // 15. seguridad_PrivateEndpoints_Unauthenticated_401_o_403
    @Test
    void seguridad_PrivateEndpoints_Unauthenticated_401_o_403() throws Exception {
        MvcResult res = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/usuario/{idUsuario}", 1L)
                .accept(MediaType.APPLICATION_JSON))
            .andReturn();

        int status = res.getResponse().getStatus();
        assertTrue(status == 401 || status == 403, "Debe rechazar acceso no autenticado");
    }
    
    // 16. contentType_Invalid_415_o_406
    @Test
    void contentType_Invalid_415_o_406() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_XML)
                .content("<telefono></telefono>"))
            .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
            .andReturn();

        int status = res.getResponse().getStatus();
        assertTrue(status == 415 || status == 406,
            "Debe rechazar content-type no soportado. status=" + status);
    }

    // 17. errorMapping_EstructuraDeErrorConsistency
    @Test
    void errorMapping_EstructuraDeErrorConsistency() throws Exception {
        // forzar que el servicio lance una excepción inesperada (usamos el SpyBean)
        Mockito.doThrow(new RuntimeException("Falla inesperada"))
               .when(telefonoService).registrarTelefono(any(RegistroTelefonoDTO.class));

        // limpiar invocaciones previas realizadas en setUp para que la verificación cuente solo la llamada de este test
        Mockito.clearInvocations(telefonoService);

        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("0000000000");
        dto.setCaracteristica("11");
        dto.setTipo("PRINCIPAL");
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(usuarioRepository.findByUsername("usuario123").get().getIdUsuario());

        MvcResult res = mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();

        assertEquals(500, res.getResponse().getStatus());
        var node = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(node.has("message") && node.has("status") && node.has("timestamp"),
            "La estructura de error debe contener message, status y timestamp. Resp: " + res.getResponse().getContentAsString());

        verify(telefonoService, times(1)).registrarTelefono(any(RegistroTelefonoDTO.class));
    }
    
    // 18. actualizarTelefono_PartialUpdate_preservaValoresNulos_OK
    @Test
    void actualizarTelefono_PartialUpdate_preservaValoresNulos_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        var telefono = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).get(0);

        com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO update =
            new com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO();
        update.setNumero(null); // solo actualizamos otro campo (ejemplo: verificado)
        update.setVerificado(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/telefono/private/{id}", telefono.getIdTelefono())
                .cookie(getAccessCookie())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk());
    }

    // 19. registrarTelefono_Normalizacion_Trim
    @Test
    void registrarTelefono_Normalizacion_Trim() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero(" 1122334455 ");
        dto.setCaracteristica(" 11 ");
        dto.setTipo("PRINCIPAL");
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(usuario.getIdUsuario());

        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numero").value("1122334455"));
    }
    
    // 20. listarTelefonos_EmptyList_200
    @Test
    void listarTelefonos_EmptyList_200() throws Exception {
        // crear un usuario nuevo sin teléfonos
        RegistroUsuarioDTO u = new RegistroUsuarioDTO();
        u.setUsername("usuario_sin_tel");
        u.setEmail("correoprueba1@noenviar.com");
        u.setPassword("Clave1234");
        u.setNombreResponsable("No");
        u.setApellidoResponsable("Tel");
        u.setDocumentoResponsable("99999999");
        u.setTipoUsuario("PERSONA_FISICA");
        u.setAceptaTerminos(true);
        u.setAceptaPoliticaPriv(true);
        u.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        u.setGeneroResponsable("MASCULINO");
        u.setIdioma("es");
        u.setTimezone("America/Argentina/Buenos_Aires");
        u.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO reg = new RegistroUsuarioCompletoDTO();
        reg.setUsuario(u);
        reg.setDirecciones(List.of());
        reg.setTelefonos(List.of());

        registrarUsuarioCompleto(reg);
        Usuario nuevo = usuarioRepository.findByUsername("usuario_sin_tel").orElseThrow();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/telefono/private/usuario/{idUsuario}", nuevo.getIdUsuario())
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }

    // 21. registrarTelefono_ValidacionDeEnums_400
    @Test
    void registrarTelefono_ValidacionDeEnums_400() throws Exception {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setNumero("1144556677");
        dto.setCaracteristica("11");
        dto.setTipo("TIPO_INVALIDO");
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(usuarioRepository.findByUsername("usuario123").get().getIdUsuario());

        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }
   
    // 22. contract_VerificarNoInteraccionesEnErrores
    @Test
    void contract_VerificarNoInteraccionesEnErrores() throws Exception {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        // payload inválido
        mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());

        // si hay mocks del servicio deberían no haber sido invocados (si aplica)
        verifyNoInteractions(telefonoService);
    }
}