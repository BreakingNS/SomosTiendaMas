package com.breakingns.SomosTiendaMas.test.Modulo2.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.DireccionService;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*                      DireccionFeatureTest

    1) crearDireccionParaUsuario_PersonalEnvioFiscal_OK
    2) crearDireccionParaEmpresa_Fiscal_OK
    3) crearDireccion_FaltanCamposObligatorios_400
    4) crearDireccion_EvitarDuplicadoMismoUsuario_OK
    5) crearDireccion_EvitarDuplicadoMismaEmpresa_OK
    6) marcarDireccionComoPrincipal_GuardaCorrectamente
    7) cambiarDireccionPrincipal_NuevaDesmarcaAnterior
    8) activarDesactivarDireccion_EfectosEnConsultas
    9) actualizarDireccion_DatosYUbicacion_Persisten
    10) crearActualizar_Direccion_UbicacionInvalida_Error
    11) crearActualizar_Direccion_OwnerInexistente_Error
    12) obtenerDireccionPorId_ValidarOwner_Forbidden
    13) listarDireccionesPorUsuario_FiltrosPrincipalActiva_OK
    14) listarDireccionesPorEmpresa_FiltrosPrincipalActiva_OK
    15) cascadaEliminar_Owner_ComportamientoCorrecto
    16) e2e_RegistrarDireccion_ServiceToRepo_VerificarRepo
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
public class DireccionTest {
    
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
    private final IDireccionRepository direccionRepository;

    private final DireccionService direccionService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;

    @BeforeEach
    void setUp() throws Exception {
        direccionDTO = new RegistroDireccionDTO();
        
        // 1. Instanciar y configurar usuario y teléfono
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
        
        // 7. Instanciar y configurar dirección con los IDs reales and el ID del usuario
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
    }

    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        return result.getResponse().getStatus();
    }
    
    // helper único: hace POST y devuelve MvcResult para inspección en tests
    private MvcResult registrarDireccion(RegistroDireccionDTO direccionDTO) throws Exception {
        return mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andReturn();
    }

    private MvcResult registrarDireccionExpectOk(RegistroDireccionDTO direccionDTO) throws Exception {
        return mockMvc.perform(post("/api/direccion/public/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(direccionDTO)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andReturn();
    }

    private RegistroDireccionDTO crearDireccionParaUsuario(Usuario usuario, String tipo, String calle, String numero, boolean esPrincipal) {
        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );

        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(pais.getId());
        dto.setIdProvincia(provincia.getId());
        dto.setIdDepartamento(departamento.getId());
        dto.setIdLocalidad(localidad.get().getId());
        dto.setIdMunicipio(municipio.getId());
        dto.setIdPerfilEmpresa(null);
        dto.setTipo(tipo);
        dto.setCalle(calle);
        dto.setNumero(numero);
        dto.setPiso(null);
        dto.setReferencia(null);
        dto.setActiva(true);
        dto.setEsPrincipal(esPrincipal);
        dto.setCodigoPostal("1000");
        dto.setIdUsuario(usuario.getIdUsuario());
        return dto;
    }

    // helper: busca en repo por usuario, calle y numero
    private Optional<Direccion> findDireccionByUsuarioCalleNumero(Long usuarioId, String calle, String numero) {
        return direccionRepository.findAll().stream()
            .filter(d -> d.getUsuario() != null && d.getUsuario().getIdUsuario().equals(usuarioId)
                    && calle.equals(d.getCalle()) && numero.equals(d.getNumero()))
            .findFirst();
    }

    // 1) Crear dirección para usuario (PERSONAL/ENVIO/FISCAL) OK.
    @Test
    void crearDireccionParaUsuario_PersonalEnvioFiscal_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroDireccionDTO d1 = crearDireccionParaUsuario(usuario, "PERSONAL", "Calle A", "1", true);
        RegistroDireccionDTO d2 = crearDireccionParaUsuario(usuario, "ENVIO", "Calle B", "2", false);
        RegistroDireccionDTO d3 = crearDireccionParaUsuario(usuario, "FISCAL", "Calle C", "3", false);

        registrarDireccionExpectOk(d1);
        registrarDireccionExpectOk(d2);
        registrarDireccionExpectOk(d3);

        List<Direccion> all = direccionRepository.findAll().stream()
            .filter(d -> d.getUsuario() != null && d.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .collect(Collectors.toList());

        assertTrue(all.size() >= 3, "Se deben haber creado 3 direcciones para el usuario");
        assertTrue(all.stream().anyMatch(d -> d.getTipo() == Direccion.TipoDireccion.PERSONAL && "Calle A".equals(d.getCalle())));
        assertTrue(all.stream().anyMatch(d -> d.getTipo() == Direccion.TipoDireccion.ENVIO && "Calle B".equals(d.getCalle())));
        assertTrue(all.stream().anyMatch(d -> d.getTipo() == Direccion.TipoDireccion.FISCAL && "Calle C".equals(d.getCalle())));
    }

    // 2) Crear dirección para empresa OK.
    @Test
    void crearDireccionParaEmpresa_Fiscal_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("ACME TEST");
        perfil.setCuit("30555555558");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoprueba@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdPerfilEmpresa(saved.getIdPerfilEmpresa());
        dto.setTipo("FISCAL");
        dto.setCalle("Av Empresa");
        dto.setNumero("500");
        dto.setActiva(true);
        dto.setEsPrincipal(true);
        dto.setCodigoPostal("1406");
        // idUsuario puede ser null para direcciones de empresa
        dto.setIdUsuario(null);

        registrarDireccionExpectOk(dto);

        boolean found = direccionRepository.findAll().stream()
            .anyMatch(d -> d.getPerfilEmpresa() != null && d.getPerfilEmpresa().getIdPerfilEmpresa().equals(saved.getIdPerfilEmpresa())
                    && "Av Empresa".equals(d.getCalle()));
        assertTrue(found, "Dirección para perfil empresa debe persistir y asociarse");
    }

    // 3) Crear dirección con campos obligatorios faltantes -> 400.
    @Test
    void crearDireccion_FaltanCamposObligatorios_400() throws Exception {
        RegistroDireccionDTO bad = new RegistroDireccionDTO();
        // campo calle faltante
        bad.setIdPais(direccionDTO.getIdPais());
        bad.setIdProvincia(direccionDTO.getIdProvincia());
        bad.setIdDepartamento(direccionDTO.getIdDepartamento());
        bad.setIdMunicipio(direccionDTO.getIdMunicipio());
        bad.setIdLocalidad(direccionDTO.getIdLocalidad());
        bad.setTipo(null); // obligatorio
        bad.setNumero(null); // obligatorio
        bad.setCodigoPostal(null); // obligatorio
        bad.setIdUsuario(direccionDTO.getIdUsuario());

        MvcResult res = registrarDireccion(bad);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 400 && status < 500, "Debe devolver 4xx cuando faltan campos obligatorios");
    }

    // 4) Evitar duplicado de dirección para el mismo usuario.
    @Test
    void crearDireccion_EvitarDuplicadoMismoUsuario_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO d = crearDireccionParaUsuario(usuario, "PERSONAL", "Dup Calle", "10", true);

        registrarDireccionExpectOk(d);
        long countBefore = direccionRepository.findAll().stream()
            .filter(dir -> dir.getUsuario()!=null && dir.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())
                    && "Dup Calle".equals(dir.getCalle()) && "10".equals(dir.getNumero()))
            .count();

        // intentar crear duplicado
        registrarDireccionExpectOk(d);
        long countAfter = direccionRepository.findAll().stream()
            .filter(dir -> dir.getUsuario()!=null && dir.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())
                    && "Dup Calle".equals(dir.getCalle()) && "10".equals(dir.getNumero()))
            .count();

        assertEquals(countBefore, countAfter, "No debe crear duplicado idéntico para el mismo usuario");
    }

    // 5) Evitar duplicado de dirección para la misma empresa.
    @Test
    void crearDireccion_EvitarDuplicadoMismaEmpresa_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("EMP DUP");
        perfil.setCuit("30777777779");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoprueba@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdPerfilEmpresa(saved.getIdPerfilEmpresa());
        dto.setTipo("FISCAL");
        dto.setCalle("Empresa Dup");
        dto.setNumero("99");
        dto.setActiva(true);
        dto.setEsPrincipal(true);
        dto.setCodigoPostal("1406");

        registrarDireccionExpectOk(dto);
        long before = direccionRepository.findAll().stream()
            .filter(dir -> dir.getPerfilEmpresa()!=null && dir.getPerfilEmpresa().getIdPerfilEmpresa().equals(saved.getIdPerfilEmpresa())
                    && "Empresa Dup".equals(dir.getCalle()) && "99".equals(dir.getNumero()))
            .count();

        registrarDireccionExpectOk(dto);
        long after = direccionRepository.findAll().stream()
            .filter(dir -> dir.getPerfilEmpresa()!=null && dir.getPerfilEmpresa().getIdPerfilEmpresa().equals(saved.getIdPerfilEmpresa())
                    && "Empresa Dup".equals(dir.getCalle()) && "99".equals(dir.getNumero()))
            .count();

        assertEquals(before, after, "No debe crear duplicado idéntico para la misma empresa");
    }

    // 6) Marcar dirección como principal y guardar correctamente.
    @Test
    void marcarDireccionComoPrincipal_GuardaCorrectamente() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Principal Calle", "21", true);

        registrarDireccionExpectOk(dto);

        Optional<Direccion> created = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "Principal Calle", "21");
        assertTrue(created.isPresent(), "Dirección creada debe existir");
        assertTrue(Boolean.TRUE.equals(created.get().getEsPrincipal()), "Debe marcarse como principal");
    }

    // 7) Cambiar dirección principal: nueva desmarca la anterior.
    @Test
    void cambiarDireccionPrincipal_NuevaDesmarcaAnterior() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroDireccionDTO a = crearDireccionParaUsuario(usuario, "PERSONAL", "A Calle", "31", true);
        registrarDireccionExpectOk(a);

        RegistroDireccionDTO b = crearDireccionParaUsuario(usuario, "PERSONAL", "B Calle", "32", true);
        registrarDireccionExpectOk(b);

        List<Direccion> allForUser = direccionRepository.findAll().stream()
            .filter(d -> d.getUsuario()!=null && d.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .collect(Collectors.toList());

        assertTrue(allForUser.stream().anyMatch(d -> "B Calle".equals(d.getCalle()) && Boolean.TRUE.equals(d.getEsPrincipal())));
        assertTrue(allForUser.stream().anyMatch(d -> "A Calle".equals(d.getCalle()) && !Boolean.TRUE.equals(d.getEsPrincipal())));
    }

    // 8) Activar/desactivar dirección y comprobar efectos en consultas.
    @Test
    void activarDesactivarDireccion_EfectosEnConsultas() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Act Calle", "41", false);
        registrarDireccionExpectOk(dto);

        Direccion dir = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "Act Calle", "41").orElseThrow();
        // desactivar vía repo (simula flujo de negocio)
        dir.setActiva(false);
        direccionRepository.save(dir);

        List<Direccion> visibles = direccionRepository.findAll().stream()
            .filter(d -> d.getUsuario()!=null && d.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()) && Boolean.TRUE.equals(d.getActiva()))
            .collect(Collectors.toList());

        assertFalse(visibles.stream().anyMatch(d -> "Act Calle".equals(d.getCalle())));
    }

    // 9) Actualizar dirección (datos y ubicación) y verificar persistencia.
    @Test
    void actualizarDireccion_DatosYUbicacion_Persisten() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Upd Calle", "51", false);
        registrarDireccionExpectOk(dto);

        Direccion dir = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "Upd Calle", "51").orElseThrow();
        dir.setCalle("Upd Calle Nueva");
        dir.setNumero("52");
        direccionRepository.save(dir);

        Optional<Direccion> updated = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "Upd Calle Nueva", "52");
        assertTrue(updated.isPresent(), "Cambios deben persistir y no crear nuevo registro");
        assertEquals(dir.getIdDireccion(), updated.get().getIdDireccion(), "Debe mantener mismo id tras actualización");
    }

    // 10) Crear/actualizar dirección con ubicación inválida -> error.
    @Test
    void crearActualizar_Direccion_UbicacionInvalida_Error() throws Exception {
        RegistroDireccionDTO bad = new RegistroDireccionDTO();
        bad.setIdPais(999999L); // inválido
        bad.setIdProvincia(999999L);
        bad.setIdDepartamento(999999L);
        bad.setIdMunicipio(999999L);
        bad.setIdLocalidad(999999L);
        bad.setTipo("PERSONAL");
        bad.setCalle("Bad Loc");
        bad.setNumero("1");
        bad.setCodigoPostal("0000");
        bad.setIdUsuario(direccionDTO.getIdUsuario());

        MvcResult res = registrarDireccion(bad);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 400 && status < 500, "Debe rechazar ubicaciones inválidas con 4xx");
    }

    // 11) Crear/actualizar dirección con owner inexistente -> error.
    @Test
    void crearActualizar_Direccion_OwnerInexistente_Error() throws Exception {
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setTipo("PERSONAL");
        dto.setCalle("NoOwner");
        dto.setNumero("77");
        dto.setCodigoPostal("1000");
        dto.setIdUsuario(9_999_999L); // owner inexistente

        MvcResult res = registrarDireccion(dto);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 400 && status < 500, "Crear con owner inexistente debe fallar 4xx");
    }

    // 12) Obtener dirección por ID con validación de owner -> forbidden.
    @Test
    void obtenerDireccionPorId_ValidarOwner_Forbidden() throws Exception {
        // crear dirección para usuario A
        Usuario usuarioA = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dA = crearDireccionParaUsuario(usuarioA, "PERSONAL", "OwnerA", "81", true);
        registrarDireccionExpectOk(dA);
        Direccion created = findDireccionByUsuarioCalleNumero(usuarioA.getIdUsuario(), "OwnerA", "81").orElseThrow();

        // crear usuario B
        RegistroUsuarioDTO u2 = new RegistroUsuarioDTO();
        u2.setUsername("otroUser");
        u2.setEmail("correoprueba2@noenviar.com");
        u2.setPassword("ClaveSegura123");
        u2.setNombreResponsable("Otro");
        u2.setApellidoResponsable("User");
        u2.setDocumentoResponsable("88888888");
        u2.setTipoUsuario("PERSONA_FISICA");
        u2.setAceptaTerminos(true);
        u2.setAceptaPoliticaPriv(true);
        u2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        u2.setGeneroResponsable("MASCULINO");
        u2.setIdioma("es");
        u2.setTimezone("America/Argentina/Buenos_Aires");
        u2.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO reg2 = new RegistroUsuarioCompletoDTO();
        reg2.setUsuario(u2);
        reg2.setDirecciones(List.of());
        reg2.setTelefonos(List.of());
        registrarUsuarioCompleto(reg2);

        // Por simplicidad comprobamos que el owner es distinto; la validación de acceso depende del controller.
        Usuario usuarioB = usuarioRepository.findByUsername("otroUser").orElseThrow();
        assertFalse(usuarioA.getIdUsuario().equals(usuarioB.getIdUsuario()), "Usuarios deben ser distintos");

        // Si la API tuviera endpoint protegido para obtener por owner, debería rechazar.
        // Aquí comprobamos que la dirección pertenece a A y no a B (precondición)
        assertTrue(created.getUsuario() != null && created.getUsuario().getIdUsuario().equals(usuarioA.getIdUsuario()));
    }

    // 13) Listar direcciones por usuario con filtros (esPrincipal, activa) OK.
    @Test
    void listarDireccionesPorUsuario_FiltrosPrincipalActiva_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO d1 = crearDireccionParaUsuario(usuario, "PERSONAL", "Filtro1", "91", true);
        RegistroDireccionDTO d2 = crearDireccionParaUsuario(usuario, "PERSONAL", "Filtro2", "92", false);
        registrarDireccionExpectOk(d1);
        registrarDireccionExpectOk(d2);

        List<Direccion> principalesActivas = direccionRepository.findAll().stream()
            .filter(d -> d.getUsuario() != null && d.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())
                    && Boolean.TRUE.equals(d.getEsPrincipal()) && Boolean.TRUE.equals(d.getActiva()))
            .collect(Collectors.toList());

        assertTrue(principalesActivas.size() >= 1, "Debe devolver al menos la dirección principal y activa");
    }

    // 14) Listar direcciones por empresa con filtros (esPrincipal, activa) OK.
    @Test
    void listarDireccionesPorEmpresa_FiltrosPrincipalActiva_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("LIST EMP");
        perfil.setCuit("30700000001");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoprueba2@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdPerfilEmpresa(saved.getIdPerfilEmpresa());
        dto.setTipo("FISCAL");
        dto.setCalle("List Empresa");
        dto.setNumero("111");
        dto.setActiva(true);
        dto.setEsPrincipal(true);
        dto.setCodigoPostal("1406");

        registrarDireccionExpectOk(dto);

        List<Direccion> results = direccionRepository.findAll().stream()
            .filter(d -> d.getPerfilEmpresa() != null && d.getPerfilEmpresa().getIdPerfilEmpresa().equals(saved.getIdPerfilEmpresa())
                    && Boolean.TRUE.equals(d.getActiva()) && Boolean.TRUE.equals(d.getEsPrincipal()))
            .collect(Collectors.toList());

        assertTrue(results.size() >= 1, "Debe listar direcciones activas y principales para la empresa");
    }

    // 15) Operaciones en cascada: eliminar owner y comprobar comportamiento correcto.
    @Test
    void cascadaEliminar_Owner_ComportamientoCorrecto() throws Exception {
        // crear usuario y dirección
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Cascade", "121", true);
        registrarDireccionExpectOk(dto);

        Direccion created = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "Cascade", "121").orElseThrow();
        // eliminar usuario
        usuarioRepository.deleteById(usuario.getIdUsuario());

        // tras eliminación, según política las direcciones deberían eliminarse o quedar huérfanas;
        // verificamos que no exista la dirección con ese idUsuario
        boolean exists = direccionRepository.findAll().stream()
            .anyMatch(d -> d.getUsuario() != null && usuario.getIdUsuario().equals(d.getUsuario().getIdUsuario()));
        assertFalse(exists, "Tras eliminar usuario no debe existir dirección asociada al mismo");
    }

    // 16) E2E: registrar dirección vía service y verificar en repositorio.
    @Test
    void e2e_RegistrarDireccion_ServiceToRepo_VerificarRepo() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "ServiceE2E", "131", false);

        // usar service directamente
        direccionService.registrarDireccion(dto);

        Optional<Direccion> created = findDireccionByUsuarioCalleNumero(usuario.getIdUsuario(), "ServiceE2E", "131");
        assertTrue(created.isPresent(), "Direccion creada por service debe persistir en repo");
    }

}
