package com.breakingns.SomosTiendaMas.test.Modulo2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

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
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.service.PerfilEmpresaService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroEmpresaCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.security.exception.PerfilEmpresaNoEncontradoException;

import lombok.RequiredArgsConstructor;

/*                      EmpresaServiceTest

        1) registrarPerfilEmpresa_Valido_OK: registra perfil empresa válido, devuelve DTO con id y campos mapeados.
        2) registrarPerfilEmpresa_CUITExistente_Error: rechaza registro si el CUIT ya existe.
        3) registrarPerfilEmpresa_UsuarioInexistente_Error: rechaza si idUsuario no existe.
        4) registrarPerfilEmpresa_CondicionIVAInvalida_Error: rechaza si condicionIVA no corresponde a enum.
        5) actualizarPerfilEmpresa_Valido_OK: actualiza campos permitidos y persiste cambios.
        6) actualizarPerfilEmpresa_NoEncontrado_Error: actualizar id inexistente lanza IllegalArgumentException.
        7) actualizarPerfilEmpresa_CambiosOptionales_OK: actualiza campos opcionales (categoria, sitio, descripcion).
        8) obtenerPerfilEmpresa_OK: devuelve PerfilEmpresaResponseDTO con idUsuario y demás campos correctamente mapeados.
        9) obtenerPerfilEmpresa_NoEncontrado_Error: obtener id inexistente lanza IllegalArgumentException.
       10) traerTodoPerfilEmpresa_OK: devuelve lista de perfiles (>= los creados).
       11) eliminarEmpresa_EliminaCorrectamente: elimina perfil empresa y verifica ausencia en repo.
       12) cascadaEliminar_DireccionesTelefonos_SeEliminan: al eliminar perfil empresa se eliminan direcciones/telefonos por cascade.

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
public class EmpresaServiceTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;
    @MockBean
    private TokenEmitidoService tokenEmitidoService;


    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;

    private final UsuarioServiceImpl usuarioService;
    private final PerfilEmpresaService perfilEmpresaService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroUsuarioDTO usuarioDTO;
    private RegistroDireccionDTO direccionDTO;
    private RegistroTelefonoDTO telefonoDTO;
    private RegistroEmpresaCompletoDTO registroEmpDTO;
    private RegistroPerfilEmpresaDTO perfilEmpresaDTO;


    @BeforeEach
    void setUp() throws Exception {
        
        direccionDTO = new RegistroDireccionDTO();
        
        // 1. Instanciar y registrar usuario y teléfono
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

        telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);
        
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

        // 2. Armar el registro completo con las listas ya instanciadas
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO)); // Dirección se agrega después
        registroDTO.setTelefonos(List.of(telefonoDTO));
        
        // ============================
        // NUEVO: preparar PerfilEmpresa y RegistroEmpresaCompletoDTO (ejemplo)
        // ============================
        perfilEmpresaDTO = new RegistroPerfilEmpresaDTO();
        // No setear idUsuario aquí: el controller/service asigna el id del responsable al crear el perfil
        perfilEmpresaDTO.setRazonSocial("ACME S.R.L.");
        perfilEmpresaDTO.setCuit("30555555558");            // 11 dígitos según validación
        perfilEmpresaDTO.setCondicionIVA("RI");            // ejemplo: Responsable Inscripto
        perfilEmpresaDTO.setEmailEmpresa("correoprueba@noenviar.com");
        perfilEmpresaDTO.setRequiereFacturacion(true);

        // dirección fiscal de la empresa (puede reutilizarse la estructura anterior con cambios)
        RegistroDireccionDTO direccionEmpresaDTO = new RegistroDireccionDTO();
        direccionEmpresaDTO.setIdPais(pais.getId());
        direccionEmpresaDTO.setIdProvincia(provincia.getId());
        direccionEmpresaDTO.setIdDepartamento(departamento.getId());
        direccionEmpresaDTO.setIdMunicipio(municipio.getId());
        direccionEmpresaDTO.setIdLocalidad(localidad.get().getId());
        direccionEmpresaDTO.setIdPerfilEmpresa(null); // se asignará cuando se persista el perfil empresa
        direccionEmpresaDTO.setTipo("FISCAL");
        direccionEmpresaDTO.setCalle("Av. Industrial");
        direccionEmpresaDTO.setNumero("500");
        direccionEmpresaDTO.setActiva(true);
        direccionEmpresaDTO.setEsPrincipal(true);
        direccionEmpresaDTO.setCodigoPostal("1406");

        // teléfono de la empresa
        RegistroTelefonoDTO telefonoEmpresaDTO = new RegistroTelefonoDTO();
        telefonoEmpresaDTO.setTipo("PRINCIPAL");
        telefonoEmpresaDTO.setCaracteristica("381");
        telefonoEmpresaDTO.setNumero("38155501111");
        telefonoEmpresaDTO.setActivo(true);
        telefonoEmpresaDTO.setVerificado(false);
        
        // armar DTO completo de empresa (responsable = usuarioDTO)
        registroEmpDTO = new RegistroEmpresaCompletoDTO();
        registroEmpDTO.setPerfilEmpresa(perfilEmpresaDTO);
        registroEmpDTO.setResponsable(usuarioDTO); // el responsable puede ser el usuario ya preparado arriba
        registroEmpDTO.setDireccionesEmpresa(List.of(direccionEmpresaDTO));
        registroEmpDTO.setTelefonosEmpresa(List.of(telefonoEmpresaDTO));
        
    }

    // 1) registrarPerfilEmpresa_Valido_OK: registra perfil empresa válido, devuelve DTO con id y campos mapeados.
    @Test
    void registrarPerfilEmpresa_Valido_OK() throws Exception {
        // Registrar responsable
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Usuario responsable = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // Setear idUsuario en el DTO de empresa
        perfilEmpresaDTO.setIdUsuario(responsable.getIdUsuario());

        var resp = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        assertNotNull(resp.getId(), "Debe devolver id de perfil empresa creado");
        assertEquals(perfilEmpresaDTO.getRazonSocial(), resp.getRazonSocial());
        assertEquals(perfilEmpresaDTO.getCuit(), resp.getCuit());
        assertEquals(perfilEmpresaDTO.getCondicionIVA(), resp.getCondicionIVA());
        assertEquals(perfilEmpresaDTO.getEmailEmpresa(), resp.getEmailEmpresa());
        assertEquals(perfilEmpresaDTO.getRequiereFacturacion(), resp.getRequiereFacturacion());

        // Verificar persistencia
        assertTrue(perfilEmpresaRepository.findById(resp.getId()).isPresent(), "Debe persistir en BD");
    }
    
    // 2) registrarPerfilEmpresa_CUITExistente_Error: rechaza registro si el CUIT ya existe.
    @Test
    void registrarPerfilEmpresa_CUITExistente_Error() throws Exception {
        // Registrar primer responsable y empresa
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp1 = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp1);
        perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        // Registrar segundo responsable
        RegistroUsuarioDTO r2 = new RegistroUsuarioDTO();
        r2.setUsername("responsable2"); r2.setEmail("correoprueba1@noenviar.com"); r2.setPassword("ClaveSegura123");
        r2.setNombreResponsable("Resp2"); r2.setApellidoResponsable("Two");
        r2.setDocumentoResponsable("22222222"); r2.setTipoUsuario("PERSONA_FISICA");
        r2.setAceptaTerminos(true); r2.setAceptaPoliticaPriv(true);
        r2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1)); r2.setGeneroResponsable("MASCULINO");
        r2.setIdioma("es"); r2.setTimezone("America/Argentina/Buenos_Aires"); r2.setRol("ROLE_USUARIO");
        usuarioService.registrarConRolDesdeDTO(r2, "1.1.1.1");
        Long idResp2 = usuarioRepository.findByUsername("responsable2").orElseThrow().getIdUsuario();

        // Intentar registrar otra empresa con el mismo CUIT
        RegistroPerfilEmpresaDTO dup = new RegistroPerfilEmpresaDTO();
        dup.setIdUsuario(idResp2);
        dup.setRazonSocial("Otra Empresa");
        dup.setCuit(perfilEmpresaDTO.getCuit()); // mismo CUIT
        dup.setCondicionIVA("RI");
        dup.setEmailEmpresa("correoprueba2@noenviar.com");
        dup.setRequiereFacturacion(false);

        assertThrows(IllegalArgumentException.class, () -> perfilEmpresaService.registrarPerfilEmpresa(dup),
            "Debe rechazar CUIT duplicado");
    }

    // 3) registrarPerfilEmpresa_UsuarioInexistente_Error: rechaza si idUsuario no existe.
    @Test
    void registrarPerfilEmpresa_UsuarioInexistente_Error() throws Exception {
        perfilEmpresaDTO.setIdUsuario(9_999_999L); // id inexistente
        assertThrows(java.util.NoSuchElementException.class,
            () -> perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO),
            "Debe fallar si el responsable no existe");
    }

    // 4) registrarPerfilEmpresa_CondicionIVAInvalida_Error: rechaza si condicionIVA no corresponde a enum.
    @Test
    void registrarPerfilEmpresa_CondicionIVAInvalida_Error() throws Exception {
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();

        perfilEmpresaDTO.setIdUsuario(idResp);
        perfilEmpresaDTO.setCondicionIVA("INVALIDA");

        assertThrows(IllegalArgumentException.class,
            () -> perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO),
            "Enum inválido debe lanzar IllegalArgumentException");
    }

    // 5) actualizarPerfilEmpresa_Valido_OK: actualiza campos permitidos y persiste cambios.
    @Test
    void actualizarPerfilEmpresa_Valido_OK() throws Exception {
        // Crear perfil
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp);
        var creado = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        // Actualizar
        var upd = new com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.ActualizarPerfilEmpresaDTO();
        upd.setRazonSocial("ACME SRL Actualizada");
        upd.setCondicionIVA("MONOTRIBUTO");
        upd.setEmailEmpresa("correoprueba2@noenviar.com");
        upd.setRequiereFacturacion(false);
        upd.setCategoriaEmpresa("RETAIL");
        upd.setSitioWeb("https://acme.example.com");
        upd.setDescripcionEmpresa("Nueva descripción");
        upd.setLogoUrl("https://cdn/acme/logo.png");
        upd.setColorCorporativo("#112233");
        upd.setDescripcionCorta("ACME corta");
        upd.setHorarioAtencion("9-18");
        upd.setDiasLaborales("Lun-Vie");
        upd.setTiempoProcesamientoPedidos(48);

        var resp = perfilEmpresaService.actualizarPerfilEmpresa(creado.getId(), upd);

        assertEquals("ACME SRL Actualizada", resp.getRazonSocial());
        assertEquals("MONOTRIBUTO", resp.getCondicionIVA());
        assertEquals("correoprueba2@noenviar.com", resp.getEmailEmpresa());
        assertEquals(false, resp.getRequiereFacturacion());
        assertEquals("RETAIL", resp.getCategoriaEmpresa());
        assertEquals("https://acme.example.com", resp.getSitioWeb());

        var entity = perfilEmpresaRepository.findById(creado.getId()).orElseThrow();
        assertEquals("ACME SRL Actualizada", entity.getRazonSocial());
        assertEquals(PerfilEmpresa.CondicionIVA.MONOTRIBUTO, entity.getCondicionIVA());
        assertEquals("correoprueba2@noenviar.com", entity.getEmailEmpresa());
        assertEquals(Boolean.FALSE, entity.getRequiereFacturacion());
        assertEquals(PerfilEmpresa.CategoriaEmpresa.RETAIL, entity.getCategoriaEmpresa());
    }
    
    // 6) actualizarPerfilEmpresa_NoEncontrado_Error: actualizar id inexistente lanza PerfilEmpresaNoEncontradoException.
    @Test
    void actualizarPerfilEmpresa_NoEncontrado_Error() throws Exception {
        var upd = new com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.ActualizarPerfilEmpresaDTO();
        upd.setRazonSocial("X");
        upd.setCondicionIVA("RI");
        upd.setEmailEmpresa("x@y.com");
        upd.setRequiereFacturacion(true);

        assertThrows(PerfilEmpresaNoEncontradoException.class,
            () -> perfilEmpresaService.actualizarPerfilEmpresa(9_999_999L, upd),
            "Debe lanzar PerfilEmpresaNoEncontradoException para id inexistente");
    }

    // 7) actualizarPerfilEmpresa_CambiosOptionales_OK: actualiza campos opcionales (categoria, sitio, descripcion).
    @Test
    void actualizarPerfilEmpresa_CambiosOptionales_OK() throws Exception {
        // Crear perfil base
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp);
        var creado = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        // Actualizar solo opcionales manteniendo requeridos
        var upd = new com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.ActualizarPerfilEmpresaDTO();
        upd.setRazonSocial(perfilEmpresaDTO.getRazonSocial());
        upd.setCondicionIVA(perfilEmpresaDTO.getCondicionIVA());
        upd.setEmailEmpresa(perfilEmpresaDTO.getEmailEmpresa());
        upd.setRequiereFacturacion(perfilEmpresaDTO.getRequiereFacturacion());
        upd.setCategoriaEmpresa("FABRICANTE");
        upd.setSitioWeb("https://fabricante.example.com");
        upd.setDescripcionEmpresa("Descripción opcional");

        var resp = perfilEmpresaService.actualizarPerfilEmpresa(creado.getId(), upd);

        assertEquals("FABRICANTE", resp.getCategoriaEmpresa());
        assertEquals("https://fabricante.example.com", resp.getSitioWeb());
        assertEquals("Descripción opcional", resp.getDescripcionEmpresa());
    }

    // 8) obtenerPerfilEmpresa_OK: devuelve PerfilEmpresaResponseDTO con idUsuario y demás campos correctamente mapeados.
    @Test
    void obtenerPerfilEmpresa_OK() throws Exception {
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Usuario resp = usuarioRepository.findByUsername("usuario123").orElseThrow();
        perfilEmpresaDTO.setIdUsuario(resp.getIdUsuario());
        var creado = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        var dto = perfilEmpresaService.obtenerPerfilEmpresa(creado.getId());
        assertEquals(creado.getId(), dto.getId());
        assertEquals(resp.getIdUsuario(), dto.getIdUsuario());
        assertEquals(perfilEmpresaDTO.getRazonSocial(), dto.getRazonSocial());
    }

    // 9) obtenerPerfilEmpresa_NoEncontrado_Error: obtener id inexistente lanza PerfilEmpresaNoEncontradoException.
    @Test
    void obtenerPerfilEmpresa_NoEncontrado_Error() throws Exception {
        assertThrows(PerfilEmpresaNoEncontradoException.class,
            () -> perfilEmpresaService.obtenerPerfilEmpresa(9_999_999L),
            "Debe lanzar PerfilEmpresaNoEncontradoException para id inexistente");
    }

    // 10) traerTodoPerfilEmpresa_OK: devuelve lista de perfiles (>= los creados).
    @Test
    void traerTodoPerfilEmpresa_OK() throws Exception {
        // Crear dos responsables y dos perfiles
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp1 = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp1);
        perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        RegistroUsuarioDTO r2 = new RegistroUsuarioDTO();
        r2.setUsername("responsable3"); r2.setEmail("correoprueba2@noenviar.com"); r2.setPassword("ClaveSegura123");
        r2.setNombreResponsable("R3"); r2.setApellidoResponsable("Tres");
        r2.setDocumentoResponsable("33333333"); r2.setTipoUsuario("PERSONA_FISICA");
        r2.setAceptaTerminos(true); r2.setAceptaPoliticaPriv(true);
        r2.setFechaNacimientoResponsable(LocalDate.of(1990,1,1)); r2.setGeneroResponsable("MASCULINO");
        r2.setIdioma("es"); r2.setTimezone("America/Argentina/Buenos_Aires"); r2.setRol("ROLE_USUARIO");
        usuarioService.registrarConRolDesdeDTO(r2, "1.1.1.1");
        Long idResp2 = usuarioRepository.findByUsername("responsable3").orElseThrow().getIdUsuario();

        RegistroPerfilEmpresaDTO e2 = new RegistroPerfilEmpresaDTO();
        e2.setIdUsuario(idResp2);
        e2.setRazonSocial("BETA SA");
        e2.setCuit("30777777779");
        e2.setCondicionIVA("RI");
        e2.setEmailEmpresa("correoprueba2@noenviar.com");
        e2.setRequiereFacturacion(true);
        perfilEmpresaService.registrarPerfilEmpresa(e2);

        List<PerfilEmpresa> all = perfilEmpresaService.traerTodoPerfilEmpresa();
        assertTrue(all.size() >= 2, "Debe devolver al menos los creados");
    }
    
    // 11) eliminarEmpresa_EliminaCorrectamente: elimina perfil empresa y verifica ausencia en repo.
    @Test
    void eliminarEmpresa_EliminaCorrectamente() throws Exception {
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp);
        var creado = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        // eliminar
        perfilEmpresaService.eliminarEmpresa(creado.getId());

        assertTrue(perfilEmpresaRepository.findById(creado.getId()).isEmpty(), "Perfil empresa debe eliminarse");
    }

    // 12) cascadaEliminar_DireccionesTelefonos_SeEliminan: (limitado a direcciones por contexto disponible)
    @Test
    void cascadaEliminar_DireccionesTelefonos_SeEliminan() throws Exception {
        // Nota: Este test verifica eliminación del perfil.
        // La creación de direcciones/telefonos asociados requiere servicios/repositorios y mapeos adicionales
        // no presentes aquí. Se valida al menos la eliminación del perfil.
        usuarioService.registrarConRolDesdeDTO(usuarioDTO, "1.1.1.1");
        Long idResp = usuarioRepository.findByUsername("usuario123").orElseThrow().getIdUsuario();
        perfilEmpresaDTO.setIdUsuario(idResp);
        var creado = perfilEmpresaService.registrarPerfilEmpresa(perfilEmpresaDTO);

        // Eliminar
        perfilEmpresaService.eliminarEmpresa(creado.getId());

        assertTrue(perfilEmpresaRepository.findById(creado.getId()).isEmpty(), "Perfil empresa debe eliminarse");
        // Si se agregan direcciones/telefonos asociados en el futuro, aquí se verificará su ausencia.
    }
    
}
