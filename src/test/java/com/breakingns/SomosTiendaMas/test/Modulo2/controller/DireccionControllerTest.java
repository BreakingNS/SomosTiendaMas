package com.breakingns.SomosTiendaMas.test.Modulo2.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.DireccionService;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.IDireccionService;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.OwnerNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

/*                      DireccionControllerTest

        1) registrarDireccion_OK_200_y_body
           - POST /direccion/public con DTO válido -> 200 y body contiene id/fields devueltos por el servicio. Verificar que IDireccionService.registrar(...) fue invocado.

        2) registrarDireccion_InvalidPayload_400_y_no_invocaService
           - POST con payload inválido (faltan campos @Valid) -> 400 y IDireccionService NO es llamado.

        3) registrarDireccion_ServiceValidation_400
           - Service mock lanza ValidationException -> controller debe mapear a 400 y devolver mensaje consistente.

        4) registrarDireccion_NotFoundOwner_404
           - Service mock lanza NotFoundException (usuario/empresa no existe) -> controller devuelve 404.

        5) registrarDirecciones_Multiple_OK_200_listaDevuelta
           - POST /direccion/public/batch con lista válida -> 200 y lista en body; verificar interacción con servicio.

        6) registrarDirecciones_ListaConItemInvalido_400
           - POST batch donde al menos un item no valida -> 400 y error claro; service no procesa/solo procesa según contrato.

        7) actualizarDireccion_OK_200
           - PUT /direccion/{id} con DTO válido -> 200 y body con resultado; verificar que service.actualizar(id, dto) fue llamado.

        8) actualizarDireccion_IdNoValido_400
           - PUT con path id no numérico -> 400 y service no invocado.

        9) actualizarDireccion_NotFound_404
           - Service mock lanza NotFoundException al actualizar -> controller devuelve 404.

        10) obtenerDireccion_OK_200_y_body
           - GET /direccion/{id} donde service devuelve DTO -> 200 y body correcto.

        11) obtenerDireccion_NotFound_404
           - GET para id inexistente -> 404.

        12) listarDireccionesPorUsuario_OK_200_lista
           - GET /direccion/usuario/{idUsuario}?esPrincipal=&activa= -> 200 y lista (vacía o con elementos) devuelta por service; verificar parámetros pasados.

        13) listarDireccionesPorUsuario_IdNoValido_400
           - Path var no numérico -> 400.

        14) listarDireccionesPorEmpresa_OK_200_lista
           - GET /direccion/empresa/{idPerfilEmpresa} -> 200 y lista; verificar que service fue llamado con id correcto.

        15) seguridad_PrivateEndpoints_Unauthenticated_401_o_403
           - Acceso a endpoints /private sin auth -> respuesta 401 (o 403 según configuración). Usar SecurityMockMvcRequestPostProcessors para tests auth.

        16) contentType_Invalid_415_o_406
           - Envío con Content-Type no soportado (ej. application/xml) -> 415/406 según configuración; verificar manejo.

        17) errorMapping_EstructuraDeErrorConsistency
           - Forzar excepción inesperada en service -> controller devuelve estructura de error consistente (status/message/timestamp) y código 500 o mapeado.

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
public class DireccionControllerTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;

    @MockBean
    private IDireccionService direccionServiceInterface;

    // También mockear la implementación concreta porque otros controllers la inyectan directamente
    @MockBean
    private DireccionService direccionService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;
    private RegistroUsuarioDTO usuarioDTO;

    private Cookie cachedAccessCookie;
    private String cachedBearerToken;

    @BeforeEach
    void setUp() throws Exception {
        // resetear mocks para evitar inter-test leakage
        Mockito.reset(direccionService, direccionServiceInterface);

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
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());

        // 3. Registrar usuario antes de registrar dirección
        registrarUsuarioCompleto(registroDTO);

        // 4. Verificar usuario y email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
        
        // 6. Buscar por nombre y obtener el ID real de cada entidad de ubicación
        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );
        if (localidad.isPresent()) {
            direccionDTO.setIdLocalidad(localidad.get().getId());
        }
        
        // 7. Instanciar y configurar dirección con los IDs reales y el ID del usuario
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.get().getId());
        direccionDTO.setIdPerfilEmpresa(null); // No es empresa
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setPiso(null); // Opcional
        direccionDTO.setReferencia(null); // Opcional
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");
        direccionDTO.setIdUsuario(usuario.getIdUsuario()); // ID del usuario

        //direccionService.registrarDireccion(direccionDTO);
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
        p.setEmailEmpresa("no@noenviar.com");
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
    

    // 1) registrarDireccion_OK_200_y_body
    @Test
    void registrarDireccion_OK_200_y_body() throws Exception {
        DireccionResponseDTO respuestaDto = new DireccionResponseDTO();
        respuestaDto.setIdDireccion(1L);
        respuestaDto.setCalle(direccionDTO.getCalle());
        respuestaDto.setNumero(direccionDTO.getNumero());
        // stubbear el mock que el controller realmente inyecta (IDireccionService)
        when(direccionServiceInterface.registrarDireccion(any(RegistroDireccionDTO.class)))
            .thenReturn(respuestaDto);

        // Act: capturar resultado para inspección
        MvcResult result = mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andExpect(status().isOk())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        // Depuración: asegúrate que el body no esté vacío
        assertFalse(body == null || body.isBlank(), "El body de la respuesta no debe estar vacío. Resp: " + body);

        // parsear y verificar campos
        DireccionResponseDTO resp = objectMapper.readValue(body, DireccionResponseDTO.class);
        assertEquals(1L, resp.getIdDireccion());
        assertEquals(direccionDTO.getCalle(), resp.getCalle());
        assertEquals(direccionDTO.getNumero(), resp.getNumero());

        verify(direccionServiceInterface, times(1)).registrarDireccion(any(RegistroDireccionDTO.class));
    }
    
    // 2) registrarDireccion_InvalidPayload_400_y_no_invocaService
    @Test
    void registrarDireccion_InvalidPayload_400_y_no_invocaService() throws Exception {
        RegistroDireccionDTO invalida = new RegistroDireccionDTO();
        invalida.setIdUsuario(direccionDTO.getIdUsuario());
        invalida.setTipo(null); // falta requerido
        invalida.setCalle(null); // falta requerido

        mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalida)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(direccionServiceInterface);
    }
    
    // 3) registrarDireccion_ServiceValidation_400
    @Test
    void registrarDireccion_ServiceValidation_400() throws Exception {
        // stubbear el mock que el controller inyecta para lanzar una excepción de validación
        when(direccionServiceInterface.registrarDireccion(any(RegistroDireccionDTO.class)))
            .thenThrow(new IllegalArgumentException("Validación de dirección"));

        mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andExpect(status().isBadRequest());

        verify(direccionServiceInterface, times(1)).registrarDireccion(any(RegistroDireccionDTO.class));
    }

    // 4) registrarDireccion_NotFoundOwner_404
    @Test
    void registrarDireccion_NotFoundOwner_404() throws Exception {
        // mockea la excepción que ControllerAdvice mapea a 404
        when(direccionServiceInterface.registrarDireccion(any(RegistroDireccionDTO.class)))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Owner no encontrado"));

        // Act + Assert: controller debe retornar 404
        mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andExpect(status().isNotFound());

        verify(direccionServiceInterface, times(1)).registrarDireccion(any(RegistroDireccionDTO.class));
    }
    /*
    // 5) registrarDirecciones_Multiple_OK_200_listaDevuelta
    @Test
    void registrarDirecciones_Multiple_OK_200_listaDevuelta() throws Exception {
        List<RegistroDireccionDTO> lista = List.of(direccionDTO);
        List<Direccion> retornoServicio = List.of(new Direccion());
        when(direccionService.registrarMultiple(anyList())).thenReturn(retornoServicio);

        mockMvc.perform(post("/api/direccion/public/registrar-multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lista)))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(direccionService, times(1)).registrarMultiple(anyList());
        verifyNoInteractions(direccionServiceInterface);
    }*/

    // 6) registrarDirecciones_ListaConItemInvalido_400
    @Test
    void registrarDirecciones_ListaConItemInvalido_400() throws Exception {
        
        direccionDTO.setTipo(null);
        
        mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("tipo")));
 
         // No debería invocarse el servicio en caso de payload inválido
         verifyNoInteractions(direccionService, direccionServiceInterface);
    }
    
    // 7) actualizarDireccion_OK_200 (usar login real y cookie de accessToken)
    @Test
    void actualizarDireccion_OK_200_conTokenPersistido() throws Exception {
        DireccionResponseDTO updatedDto = new DireccionResponseDTO();
        updatedDto.setIdDireccion(10L);
        updatedDto.setCalle("Nueva Calle");

        when(direccionServiceInterface.actualizarDireccion(eq(10L), any(ActualizarDireccionDTO.class)))
            .thenReturn(updatedDto);

        ActualizarDireccionDTO updateDto = new ActualizarDireccionDTO();
        updateDto.setCalle("Nueva Calle");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/direccion/private/{id}", 10L)
                .cookie(getAccessCookie()) // <-- usar cookie
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idDireccion").value(10));
    }
    
    // 8) actualizarDireccion_IdNoValido_400
    @Test
    void actualizarDireccion_IdNoValido_400() throws Exception {
        // obtener cookie vía helper (hace login si hace falta)
        Cookie accessCookie = getAccessCookie();

        ActualizarDireccionDTO updateDto = new ActualizarDireccionDTO();
        updateDto.setCalle("Calle X");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/direccion/private/{id}", "abc")
                .cookie(accessCookie) // <<-- asegurarse de estar autenticado para que falle por id inválido, no por 401
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(direccionServiceInterface);
    }
    
    // 9) actualizarDireccion_NotFound_404
    @Test
    void actualizarDireccion_NotFound_404() throws Exception {
        ActualizarDireccionDTO updateDto = new ActualizarDireccionDTO();
        updateDto.setCalle("Calle Inexistente");

        when(direccionServiceInterface.actualizarDireccion(eq(99L), any(ActualizarDireccionDTO.class)))
            .thenThrow(new OwnerNotFoundException("No existe"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/direccion/private/{id}", 99L)
                .header("Authorization", "Bearer " + getBearerToken()) // <-- usar bearer header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isNotFound());
    }

    // 10) obtenerDireccion_OK_200_y_body
    @Test
    void obtenerDireccion_OK_200_y_body() throws Exception {
        DireccionResponseDTO dto = new DireccionResponseDTO();
        dto.setIdDireccion(5L);
        dto.setCalle("Calle A");

        when(direccionServiceInterface.obtenerDireccion(5L)).thenReturn(dto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/{id}", 5L)
                .cookie(getAccessCookie()) // <-- enviar cookie de autenticación
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.idDireccion").value(5))
            .andExpect(jsonPath("$.calle").value("Calle A"));

        verify(direccionServiceInterface, times(1)).obtenerDireccion(5L);
    }
    
    // 11) obtenerDireccion_NotFound_404
    @Test
    void obtenerDireccion_NotFound_404() throws Exception {
        when(direccionServiceInterface.obtenerDireccion(123L))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("No existe"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/{id}", 123L)
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(direccionServiceInterface, times(1)).obtenerDireccion(123L);
    }

    // 12) listarDireccionesPorUsuario_OK_200_lista
    @Test
    void listarDireccionesPorUsuario_OK_200_lista() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        
        // devolver dos DTOs simulados
        List<DireccionResponseDTO> listaSimulada = List.of(new DireccionResponseDTO(), new DireccionResponseDTO());
        when(direccionServiceInterface.listarDireccionesPorUsuario(usuarioOpt.get().getIdUsuario()))
            .thenReturn(listaSimulada);

        MvcResult res = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/usuario/{idUsuario}", usuarioOpt.get().getIdUsuario())
                    .cookie(getAccessCookie())
                    .accept(MediaType.APPLICATION_JSON))
            .andReturn();

        assertEquals(200, res.getResponse().getStatus());
        verify(direccionServiceInterface, times(1)).listarDireccionesPorUsuario(usuarioOpt.get().getIdUsuario());
    }
    
    // 13) listarDireccionesPorUsuario_IdNoValido_400
    @Test
    void listarDireccionesPorUsuario_IdNoValido_400() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/usuario/{idUsuario}", "x")
                .cookie(getAccessCookie()) // asegurarse autenticación para que la falla sea por conversión de id
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("idUsuario")));

        // verificar el mock que inyecta el controller, no el repositorio/impl concreto
        verifyNoInteractions(direccionServiceInterface);
    }

    // 14) listarDireccionesPorEmpresa_OK_200_lista
    @Test
    void listarDireccionesPorEmpresa_OK_200_lista() throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");

        // persistir un perfil de empresa de prueba (usa helper de la clase)
        PerfilEmpresa perfil = crearPerfilEmpresa(usuarioOpt.get(), "EmpresaPrioridad", "20999888777");

        // preparar respuesta simulada del servicio (usa el mock que inyecta el controller)
        List<DireccionResponseDTO> listaSimulada = List.of(new DireccionResponseDTO(), new DireccionResponseDTO());
        when(direccionServiceInterface.listarDireccionesPorPerfilEmpresa(perfil.getIdPerfilEmpresa()))
            .thenReturn(listaSimulada);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/empresa/{idPerfilEmpresa}", perfil.getIdPerfilEmpresa())
                .cookie(getAccessCookie())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(direccionServiceInterface, times(1)).listarDireccionesPorPerfilEmpresa(perfil.getIdPerfilEmpresa());
    }
    
    // 15) seguridad_PrivateEndpoints_Unauthenticated_401_o_403
    @Test
    void seguridad_PrivateEndpoints_Unauthenticated_401_o_403() throws Exception {
        MvcResult res = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/direccion/private/{id}", 1L))
            .andReturn();

        int status = res.getResponse().getStatus();
        assertTrue(status == 401 || status == 403, "Debe rechazar acceso no autenticado");
    }
    
    // 16) contentType_Invalid_415_o_406
    @Test
    void contentType_Invalid_415_o_406() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_XML)
                .content("<direccion></direccion>"))
            .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print()) // imprime request/response en consola
            .andReturn();

        int status = res.getResponse().getStatus();
        String respBody = res.getResponse().getContentAsString();
        String respContentType = res.getResponse().getContentType();

        assertTrue(status == 415 || status == 406,
            "Debe rechazar content-type no soportado. status=" + status
            + ", responseContentType=" + respContentType
            + ", body=" + (respBody == null || respBody.isBlank() ? "<empty>" : respBody));

        // asegurar que no se llegó a invocar el servicio
        verifyNoInteractions(direccionServiceInterface);
        verifyNoInteractions(direccionService);
    }
    
    // 17) errorMapping_EstructuraDeErrorConsistency
    @Test
    void errorMapping_EstructuraDeErrorConsistency() throws Exception {
        when(direccionServiceInterface.registrarDireccion(any(RegistroDireccionDTO.class)))
            .thenThrow(new RuntimeException("Falla inesperada"));

        MvcResult res = mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andReturn();

        assertEquals(500, res.getResponse().getStatus());

        String body = res.getResponse().getContentAsString();
        var node = objectMapper.readTree(body);
        // estructura mínima esperada: message, status, timestamp
        assertTrue(node.has("message") && node.has("status") && node.has("timestamp"),
            "La estructura de error debe contener message, status y timestamp. Resp: " + body);

        verify(direccionServiceInterface, times(1)).registrarDireccion(any(RegistroDireccionDTO.class));
    }
    
}
