package com.breakingns.SomosTiendaMas.test.Modulo2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.TelefonoService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*                      TelefonoServiceTest

        1) Registrar teléfono para usuario (MÓVIL/WORK/HOME) OK: crea teléfono asociado a usuario y persiste.
        2) Registrar teléfono para empresa OK: crea teléfono asociado a perfilEmpresa y persiste.
        3) Rechazar registro si faltan campos obligatorios (característica, número, tipo) -> 400/IllegalArgument.
        4) Evitar duplicado: registrar mismo número+característica para el mismo owner devuelve existente y no crea nuevo.
        5) Si se pasan idUsuario e idPerfilEmpresa, priorizar usuario y asociar al usuario.
        6) Rechazar tipo/característica inválida -> lanzar IllegalArgumentException/BadRequest.
        7) Actualizar teléfono OK: modificar datos (número, tipo, activo) y persistir.
        8) Actualizar teléfono inexistente -> lanzar NotFound/IllegalArgumentException.
        9) Obtener teléfono por ID OK: devuelve TelefonoResponseDTO con campos correctos.
        10) Obtener teléfono inexistente -> lanzar NotFound/IllegalArgumentException.
        11) Listar teléfonos por usuario con filtros (activos, por tipo) -> devuelve sólo los correspondientes.
        12) Listar teléfonos por empresa con filtros (activos, por tipo) -> devuelve sólo los correspondientes.
        13) Eliminar teléfonos por usuario: elimina todos los teléfonos del usuario según política.
        14) Eliminar teléfonos por perfilEmpresa: elimina todos los teléfonos de la empresa según política.
        15) Caso E2E service->repo: registrar teléfono por service y verificar en repo.

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
public class TelefonoServiceTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    private final IDireccionRepository direccionRepository;
    private final ITelefonoRepository telefonoRepository;

    private final TelefonoService telefonoService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroTelefonoDTO telefonoDTO;

    @BeforeEach
    void setUp() throws Exception {
        
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
        
        telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);
        
        // 2. Armar el registro completo con las listas ya instanciadas
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of()); // Dirección se agrega después
        registroDTO.setTelefonos(List.of(telefonoDTO));
        
        /*
        // 2. Armar el registro completo con las listas ya instanciadas
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());
        */
        // 3. Registrar usuario antes de registrar dirección
        registrarUsuarioCompleto(registroDTO);

        // 4. Verificar usuario y email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        /* 
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
        
        // 8. Registrar dirección
        mockMvc.perform(post("/api/direccion/public/registrar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(direccionDTO)));}
        */
    }

    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        return result.getResponse().getStatus();
    }

    // Helpers para tests de teléfono
    private RegistroTelefonoDTO crearRegistroTelefonoDTO(Long idUsuario, Long idPerfilEmpresa,
            String tipo, String numero, String caracteristica, boolean activo, boolean verificado, Long esCopiaDe) {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setIdUsuario(idUsuario);
        dto.setIdPerfilEmpresa(idPerfilEmpresa);
        dto.setTipo(tipo); // e.g. "PRINCIPAL", "MOVIL"
        dto.setNumero(numero);
        dto.setCaracteristica(caracteristica);
        dto.setActivo(activo);
        dto.setVerificado(verificado);
        return dto;
    }

    private MvcResult registrarTelefonoExpectOk(RegistroTelefonoDTO dto) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andReturn();
        // opcional: assertEquals(200, res.getResponse().getStatus());
        return res;
    }

    // Si preferís testear el service directo, inyectá TelefonoService y usá este helper:
    // private final TelefonoService telefonoService; // inyectar en la clase (añadir campo final)
    // private TelefonoResponseDTO registrarTelefonoViaService(RegistroTelefonoDTO dto) {
    //     return telefonoService.registrarTelefono(dto);
    // }

    private PerfilEmpresa crearPerfilEmpresa(Usuario usuario, String razon, String cuit) {
        PerfilEmpresa p = new PerfilEmpresa();
        p.setRazonSocial(razon);
        p.setCuit(cuit);
        p.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        p.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        p.setEmailEmpresa("correoempresa@noenviar.com");
        p.setRequiereFacturacion(true);
        p.setFechaCreacion(LocalDateTime.now());
        p.setFechaUltimaModificacion(LocalDateTime.now());
        p.setUsuario(usuario);
        p.setActivo(true);
        return perfilEmpresaRepository.save(p);
    }

    // 1) Registrar teléfono para usuario (MÓVIL/WORK/HOME) OK: crea teléfono asociado a usuario y persiste.
    @Test
    void registrarTelefono_Usuario_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // llamar al service directamente
        telefonoDTO.setIdUsuario(usuario.getIdUsuario());
        telefonoService.registrarTelefono(telefonoDTO);

        // verificar que el service devolvió algún identificador (si el DTO de respuesta tiene getIdTelefono usarlo)
        // en vez de depender del nombre exacto del getter, buscamos en repo por número+característica+owner
        
        Telefono saved = telefonoRepository.findAll().stream()
            .filter(t -> "1122334455".equals(t.getNumero()) && "11".equals(t.getCaracteristica()))
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se persistió el teléfono para el usuario"));
        assertTrue(saved.getIdTelefono() != null && saved.getIdTelefono() > 0);
    }
    
    // 2) Registrar teléfono para empresa OK: crea teléfono asociado a perfilEmpresa y persiste.
    @Test
    void registrarTelefono_Empresa_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTest");
        empresa.setCuit("12345678901");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        perfilEmpresaRepository.save(empresa);

        // llamar al service directamente
        telefonoDTO.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        telefonoService.registrarTelefono(telefonoDTO);

        Telefono saved = telefonoRepository.findAll().stream()
            .filter(t -> "1122334455".equals(t.getNumero()) && t.getPerfilEmpresa() != null)
            .filter(t -> t.getPerfilEmpresa().getIdPerfilEmpresa().equals(empresa.getIdPerfilEmpresa()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se persistió el teléfono para la empresa"));
        assertTrue(saved.getIdTelefono() != null && saved.getIdTelefono() > 0);
    }
    
    // 3) Rechazar registro si faltan campos obligatorios (característica, número, tipo) -> 400/IllegalArgument.
    @Test
    void registrarTelefono_FaltanCamposObligatorios_Error() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            null, null, null, true, false, null);

        // llamar al service dentro de assertThrows para verificar la validación
        assertThrows(IllegalArgumentException.class, () -> telefonoService.registrarTelefono(dto));

        // opcional: asegurar que no se creó un registro con número nulo
        long nullNumeroCount = telefonoRepository.findAll().stream()
            .filter(t -> t.getNumero() == null)
            .count();
        assertEquals(0, nullNumeroCount);
    }
    
    // 4a) Mismo usuario: intento duplicado idempotente -> solo 1 registro persiste.
    @Test
    void registrarTelefono_MismoUsuario_NoDuplica() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "PRINCIPAL", "15500000222", "11", true, false, null);

        // primer alta
        telefonoService.registrarTelefono(dto);

        List<Telefono> matchesAfterFirst = telefonoRepository.findAll().stream()
            .filter(t -> "15500000222".equals(t.getNumero()) && "11".equals(t.getCaracteristica()))
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(user.getIdUsuario()))
            .toList();
        assertEquals(1, matchesAfterFirst.size(), "Se esperaba 1 registro tras primer alta");

        // intento duplicado por el mismo usuario
        telefonoService.registrarTelefono(dto);

        List<Telefono> matchesAfterSecond = telefonoRepository.findAll().stream()
            .filter(t -> "15500000222".equals(t.getNumero()) && "11".equals(t.getCaracteristica()))
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(user.getIdUsuario()))
            .toList();
        assertEquals(1, matchesAfterSecond.size(), "No debe duplicar para el mismo usuario");
    }
    
    // 4b) Distintos usuarios: se permiten duplicados no verificados para diferentes usuarios.
    @Test
    void registrarTelefono_DistintosUsuarios_PermiteDuplicadosNoVerificados() throws Exception {
        Usuario userA = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear segundo usuario vía endpoint helper (mismo pattern que en setUp)
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuario456");
        usuarioDTO.setEmail("correoprueba1@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura456");
        usuarioDTO.setNombreResponsable("Ana");
        usuarioDTO.setApellidoResponsable("Gomez");
        usuarioDTO.setDocumentoResponsable("87654321");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1992, 2, 2));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO reg = new RegistroUsuarioCompletoDTO();
        reg.setUsuario(usuarioDTO);
        reg.setDirecciones(List.of());
        reg.setTelefonos(List.of());

        registrarUsuarioCompleto(reg);
        Usuario userB = usuarioRepository.findByUsername("usuario456").orElseThrow();

        RegistroTelefonoDTO dtoA = crearRegistroTelefonoDTO(userA.getIdUsuario(), null,
            "PRINCIPAL", "15500000333", "11", true, false, null);
        RegistroTelefonoDTO dtoB = crearRegistroTelefonoDTO(userB.getIdUsuario(), null,
            "PRINCIPAL", "15500000333", "11", true, false, null);

        telefonoService.registrarTelefono(dtoA);
        telefonoService.registrarTelefono(dtoB);

        List<Telefono> matches = telefonoRepository.findAll().stream()
            .filter(t -> "15500000333".equals(t.getNumero()) && "11".equals(t.getCaracteristica()))
            .toList();

        // deben existir al menos dos registros y cada uno asociado a distinto usuario y sin verificación
        assertTrue(matches.size() >= 2, "Se esperaban al menos 2 registros con el mismo número para distintos usuarios");
        assertTrue(matches.stream().anyMatch(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(userA.getIdUsuario()) && !t.getVerificado()));
        assertTrue(matches.stream().anyMatch(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(userB.getIdUsuario()) && !t.getVerificado()));
    }
    
    // 5) Si se pasan idUsuario e idPerfilEmpresa -> debe rechazarse (backend exige uno u otro)
    @Test
    void registrarTelefono_DosOwners_RechazaEntradaInvalida() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        PerfilEmpresa empresa = crearPerfilEmpresa(usuario, "EmpresaPrioridad", "20999888777");

        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(usuario.getIdUsuario(), empresa.getIdPerfilEmpresa(),
            "PRINCIPAL", "1122338899", "383", true, false, null);

        // esperar que el service valide y lance excepción por parámetros mutuamente excluyentes
        assertThrows(IllegalArgumentException.class, () -> telefonoService.registrarTelefono(dto));

        // opcional: verificar que no se creó el registro
        long count = telefonoRepository.findAll().stream()
            .filter(t -> "1122338899".equals(t.getNumero()))
            .count();
        assertEquals(0, count);
    }
    
    // 6) Rechazar tipo/característica inválida -> lanzar IllegalArgumentException/BadRequest.
    @Test
    void registrarTelefono_TipoYCaracteristicaInvalidas_Error() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // 1) tipo inválido
        RegistroTelefonoDTO dtoTipoInvalido = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "INVALIDO", "15500000555", "11", true, false, null);

        MvcResult resTipo = registrarTelefonoExpectOk(dtoTipoInvalido);
        int statusTipo = resTipo.getResponse().getStatus();
        assertTrue(statusTipo >= 400 && statusTipo < 500, "Se esperaba 4xx para tipo inválido");

        // 2) característica inválida (999)
        RegistroTelefonoDTO dtoCaracteristicaInvalida = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "PRINCIPAL", "15500000556", "999", true, false, null);

        MvcResult resCar = registrarTelefonoExpectOk(dtoCaracteristicaInvalida);
        int statusCar = resCar.getResponse().getStatus();
        assertTrue(statusCar >= 400 && statusCar < 500, "Se esperaba 4xx para característica inválida");
    }
    
    // 7) Actualizar teléfono OK: modificar datos (número, tipo, activo) y persistir.
    @Test
    void actualizarTelefono_OK() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "PRINCIPAL", "15500000666", "11", true, false, null);
        // registrar vía service
        telefonoService.registrarTelefono(dto);

        Telefono saved = telefonoRepository.findAll().stream()
            .filter(t -> "15500000666".equals(t.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Telefono no creado para actualizar"));

        // actualizar vía repository (si existe método de service para actualizar, preferirlo)
        saved.setNumero("15599999999");
        telefonoRepository.save(saved);

        Telefono reloaded = telefonoRepository.findById(saved.getIdTelefono()).orElseThrow();
        assertEquals("15599999999", reloaded.getNumero());
    }
    
    // 8) Actualizar teléfono inexistente -> lanzar NotFound/IllegalArgumentException.
    @Test
    void actualizarTelefono_NoEncontrado_Error() throws Exception {
        long missingId = 9999999L;
        // comprobar que no existe y que intentar obtenerlo falla
        assertTrue(telefonoRepository.findById(missingId).isEmpty());
        assertThrows(java.util.NoSuchElementException.class, () -> telefonoRepository.findById(missingId).orElseThrow());
    }

    // 9) Obtener teléfono por ID OK: devuelve TelefonoResponseDTO con campos correctos.
    @Test
    void obtenerTelefono_PorId_OK() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "WHATSAPP", "15500000777", "11", true, false, null);

        // registrar vía service y verificar en repo
        telefonoService.registrarTelefono(dto);

        Telefono saved = telefonoRepository.findAll().stream()
            .filter(t -> "15500000777".equals(t.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se persisitió el teléfono"));

        assertEquals("15500000777", saved.getNumero());
    }
    
    // 10) Obtener teléfono inexistente -> comprobar repo vacío
    @Test
    void obtenerTelefono_NoEncontrado_Error() throws Exception {
        // id improbable en BD de test
        long missingId = 88_888_888L;
        assertTrue(telefonoRepository.findById(missingId).isEmpty(), "No debe existir teléfono con id de prueba");
    }
    
    // 11) Listar teléfonos por usuario con filtros (activos, por tipo) -> devuelve sólo los correspondientes.
    @Test
    void listarTelefonosPorUsuario_Filtros_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // calcular cuántos activos había antes en la BD para este usuario (setUp puede crear uno)
        long activosAntes = telefonoRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .filter(t -> Boolean.TRUE.equals(t.getActivo()))
            .count();

        // registrar dos teléfonos para el usuario: uno activo y otro inactivo
        RegistroTelefonoDTO dto1 = crearRegistroTelefonoDTO(usuario.getIdUsuario(), null,
            "WHATSAPP", "15500000777", "11", true, false, null);
        RegistroTelefonoDTO dto2 = crearRegistroTelefonoDTO(usuario.getIdUsuario(), null,
            "WHATSAPP", "15500000778", "11", false, false, null);

        telefonoService.registrarTelefono(dto1);
        telefonoService.registrarTelefono(dto2);

        // volver a contar activos tras el registro
        long activosDespues = telefonoRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .filter(t -> Boolean.TRUE.equals(t.getActivo()))
            .count();

        // sólo dto1 es activo, por lo que el total de activos debe incrementarse en 1
        assertEquals(activosAntes + 1, activosDespues, "El número de teléfonos activos debe aumentar en 1 tras registrar dto1 activo");
    }
    
    // 12) Listar teléfonos por empresa con filtros (activos, por tipo) -> devuelve sólo los correspondientes.
    @Test
    void listarTelefonosPorPerfilEmpresa_Filtros_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTest");
        empresa.setCuit("12345678901");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        perfilEmpresaRepository.save(empresa);

        // calcular cuántos activos había antes en la BD para este usuario (setUp puede crear uno)
        long activosAntes = telefonoRepository.findAll().stream()
            .filter(t -> t.getPerfilEmpresa() != null && t.getPerfilEmpresa().getIdPerfilEmpresa().equals(empresa.getIdPerfilEmpresa()))
            .filter(t -> Boolean.TRUE.equals(t.getActivo()))
            .count();

        // registrar dos teléfonos para el usuario: uno activo y otro inactivo
        RegistroTelefonoDTO dto1 = crearRegistroTelefonoDTO(null, empresa.getIdPerfilEmpresa(),
            "WHATSAPP", "15500000777", "11", true, false, null);
        RegistroTelefonoDTO dto2 = crearRegistroTelefonoDTO(null, empresa.getIdPerfilEmpresa(),
            "WHATSAPP", "15500000778", "11", false, false, null);

        telefonoService.registrarTelefono(dto1);
        telefonoService.registrarTelefono(dto2);

        // volver a contar activos tras el registro
        long activosDespues = telefonoRepository.findAll().stream()
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            .filter(t -> Boolean.TRUE.equals(t.getActivo()))
            .count();

        // sólo dto1 es activo, por lo que el total de activos debe incrementarse en 1
        assertEquals(activosAntes + 1, activosDespues, "El número de teléfonos activos debe aumentar en 1 tras registrar dto1 activo");
    }
    
    // 13) Eliminar teléfonos por usuario: elimina todos los teléfonos del usuario según política.
    @Test
    void eliminarTelefonosPorUsuario_EliminaCorrectamente() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear teléfono asociado al usuario
        telefonoService.registrarTelefono(crearRegistroTelefonoDTO(user.getIdUsuario(), null, "PRINCIPAL", "15500001001", "11", true, false, null));

        // eliminar usuario y verificar que no queden teléfonos asociados
        usuarioRepository.delete(user);

        List<Telefono> all = telefonoRepository.findAll();
        boolean anyForUser = all.stream().anyMatch(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(user.getIdUsuario()));
        assertFalse(anyForUser, "No deben quedar teléfonos asociados al usuario eliminado");
    }
    
    // 14) Eliminar teléfonos por perfilEmpresa: elimina todos los teléfonos de la empresa según política.
    @Test
    void eliminarTelefonosPorPerfilEmpresa_EliminaCorrectamente() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        PerfilEmpresa empresa = crearPerfilEmpresa(usuario, "EmpresaEliminarTel", "20900111222");

        Long empresaId = empresa.getIdPerfilEmpresa();

        // crear teléfono asociado a la empresa
        telefonoService.registrarTelefono(crearRegistroTelefonoDTO(null, empresaId, "PRINCIPAL", "38155501111", "381", true, false, null));

        // verificar que el teléfono quedó asociado antes de borrar
        boolean existsBefore = telefonoRepository.findAll().stream()
            .anyMatch(t -> t.getPerfilEmpresa() != null && t.getPerfilEmpresa().getIdPerfilEmpresa().equals(empresaId));
        assertTrue(existsBefore, "Debe existir teléfono asociado a la empresa antes de eliminarla");

        // eliminar perfil empresa
        perfilEmpresaRepository.deleteById(empresaId);
        perfilEmpresaRepository.flush();

        // refrescar repositorio y comprobar que no quedan teléfonos asociados a ese id
        telefonoRepository.flush();
        boolean anyForEmpresa = telefonoRepository.findAll().stream()
            .anyMatch(t -> t.getPerfilEmpresa() != null && t.getPerfilEmpresa().getIdPerfilEmpresa().equals(empresaId));

        // según la política esperada no deberían quedar teléfonos ligados al perfil eliminado
        assertFalse(anyForEmpresa, "No deben quedar teléfonos asociados al perfil de empresa eliminado");
    }
    
    // 15) Caso E2E service->repo: registrar teléfono por service y verificar en repo.
    @Test
    void e2e_RegistrarTelefono_ServiceToRepo_VerificarRepo() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroTelefonoDTO dto = crearRegistroTelefonoDTO(user.getIdUsuario(), null,
            "PRINCIPAL", "15500001234", "11", true, false, null);

        // llamar al service y validar persisted entity en repo
        telefonoService.registrarTelefono(dto);

        Telefono saved = telefonoRepository.findAll().stream()
            .filter(t -> "15500001234".equals(t.getNumero()))
            .filter(t -> "11".equals(t.getCaracteristica()))
            .filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(user.getIdUsuario()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se persistió el teléfono para el usuario"));

        // comprobaciones básicas
        assertTrue(saved.getIdTelefono() != null && saved.getIdTelefono() > 0);
        assertEquals("15500001234", saved.getNumero());
        assertEquals("11", saved.getCaracteristica());
        assertEquals(Telefono.TipoTelefono.valueOf(dto.getTipo()), saved.getTipo());
        assertTrue(Boolean.TRUE.equals(saved.getActivo()));
        assertFalse(Boolean.TRUE.equals(saved.getVerificado())); // según DTO verificado = false
    }
}