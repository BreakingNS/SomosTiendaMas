package com.breakingns.SomosTiendaMas.test.Modulo2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/*                      DireccionServiceTest

        1) Crear dirección para usuario (PERSONAL/ENVIO/FISCAL) OK.
        2) Crear dirección para empresa OK.
        3) Rechazar creación si faltan campos obligatorios (calle, número, código postal, país, tipo).
        4) Evitar duplicado de dirección para el mismo usuario.
        5) Evitar duplicado de dirección para la misma empresa.
        6) Marcar dirección como principal y comprobar que se guarda correctamente.
        7) Cambiar principal: nueva principal desmarca la anterior.
        8) Activar/desactivar dirección y comprobar efectos en consultas.
        9) Actualizar dirección (datos y ubicación) y verificar persistencia.
        10) Crear/actualizar con IDs de ubicación inválidos -> lanzar error.
        11) Crear/actualizar con usuario/perfil empresa inexistente -> lanzar error.
        12) Obtener dirección por ID con validación de owner (usuario o empresa).
        13) Listar direcciones por usuario con filtros (esPrincipal, activa).
        14) Listar direcciones por empresa con filtros (esPrincipal, activa).
        15) Operaciones en cascada: eliminar usuario/perfil y comprobar efecto según política.
        16) Caso E2E service->repo: registrar dirección por service y verificar en repo.

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
public class DireccionServiceTest {
    
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
        direccionDTO.setUsarComoEnvio(true);    
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
        dto.setUsarComoEnvio(true);
        dto.setCodigoPostal("1000");
        dto.setIdUsuario(usuario.getIdUsuario());
        return dto;
    }

    // 1) Crear dirección para usuario (PERSONAL/ENVIO/FISCAL) OK.
    @Test
    void crearDireccionParaUsuario_PersonalEnvioFiscal_OK() throws Exception {
        // registrar vía service
        var resp = direccionService.registrarDireccion(direccionDTO);
        assertTrue(resp != null);
        assertEquals(direccionDTO.getCalle(), resp.getCalle());

        // verificar en repo que quedó persistida
        var listas = direccionRepository.findByUsuario_IdUsuario(direccionDTO.getIdUsuario());
        assertFalse(listas.isEmpty());
        assertEquals(direccionDTO.getCalle(), listas.get(0).getCalle());
    }

    // 2) Crear dirección para empresa OK.
    @Test
    void crearDireccionParaEmpresa_OK() throws Exception {
        long before = direccionRepository.count();

        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTest");
        empresa.setCuit("12345678901");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario); // Debes crear y asociar un usuario válido en un test real

        perfilEmpresaRepository.save(empresa);

        // usar los ids de ubicación ya cargados en el beforeEach (direccionDTO)
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        dto.setTipo("PERSONAL");
        dto.setCalle("Av. Empresa");
        dto.setNumero("100");
        dto.setActiva(true);
        dto.setEsPrincipal(true);
        dto.setUsarComoEnvio(true);
        dto.setCodigoPostal("5000");
        dto.setIdUsuario(null); // dirección de empresa no tiene usuario directo

        // registrar vía endpoint y verificar OK
        registrarDireccionExpectOk(dto);

        // comprobar persistencia
        long after = direccionRepository.count();
        assertEquals(before + 1, after);
    }

    // 3) Rechazar creación si faltan campos obligatorios (calle, número, código postal, país, tipo).
    @Test
    void crearDireccion_FaltanCamposObligatorios_Error() throws Exception {
                
        // construir DTO con ubicaciones válidas pero sin calle/numero
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdUsuario(direccionDTO.getIdUsuario());
        dto.setTipo(null); // falta tipo
        dto.setCalle(null); // falta calle
        dto.setNumero(null); // falta numero
        dto.setActiva(true);

        MvcResult res = registrarDireccion(dto);
        int status = res.getResponse().getStatus();
        // esperamos error de validación (400)
        assertTrue(status == 400);
    }
    
    // 4) Evitar duplicado de dirección para el mismo usuario.
    @Test
    void crearDireccion_EvitarDuplicadoMismoUsuario_OK() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // registrar la primera vez
        RegistroDireccionDTO first = crearDireccionParaUsuario(user, "PERSONAL", "Calle Duplicada", "1", true);
        registrarDireccionExpectOk(first);
        long countAfterFirst = direccionRepository.count();

        // intentar registrar la misma dirección otra vez
        RegistroDireccionDTO duplicate = crearDireccionParaUsuario(user, "PERSONAL", "Calle Duplicada", "1", true);
        MvcResult res = registrarDireccion(duplicate);
        int status = res.getResponse().getStatus();

        // según la lógica: devuelve 200 OK y NO crea una nueva fila
        assertTrue(status == 200);
        assertEquals(countAfterFirst, direccionRepository.count());

        // comprobación adicional: sólo UNA dirección con esos datos para el usuario
        long matches = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario())
            .stream()
            .filter(d -> "Calle Duplicada".equals(d.getCalle()) && "1".equals(d.getNumero()))
            .count();
        assertEquals(1, matches);
    }
     
    // 5) Evitar duplicado de dirección para la misma empresa.
    @Test
    void crearDireccion_EvitarDuplicadoMismaEmpresa_OK() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y guardar perfil empresa asociado al usuario
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
        perfilEmpresaRepository.save(empresa);

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );

        RegistroDireccionDTO direccionEmpresaDTO = new RegistroDireccionDTO();
        direccionEmpresaDTO.setIdPais(pais.getId());
        direccionEmpresaDTO.setIdProvincia(provincia.getId());
        direccionEmpresaDTO.setIdDepartamento(departamento.getId());
        direccionEmpresaDTO.setIdLocalidad(localidad.get().getId());
        direccionEmpresaDTO.setIdMunicipio(municipio.getId());
        direccionEmpresaDTO.setIdPerfilEmpresa(null);
        direccionEmpresaDTO.setTipo("FISCAL");
        direccionEmpresaDTO.setCalle("Calle Empresa");
        direccionEmpresaDTO.setNumero("789");
        direccionEmpresaDTO.setPiso(null);
        direccionEmpresaDTO.setReferencia(null);
        direccionEmpresaDTO.setActiva(true);
        direccionEmpresaDTO.setEsPrincipal(true);
        direccionEmpresaDTO.setUsarComoEnvio(true);
        direccionEmpresaDTO.setCodigoPostal("1000");

        // crear dirección para la empresa una vez (usar helper para obtener ids de ubicación)
        direccionEmpresaDTO.setIdUsuario(null); // nulo para empresa
        direccionEmpresaDTO.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        direccionService.registrarDireccion(direccionEmpresaDTO);

        // intentar crear la misma dirección otra vez
        direccionService.registrarDireccion(direccionEmpresaDTO);

        // comprobación adicional: sólo UNA dirección con esos datos para el usuario
        long matches = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa())
            .stream()
            .filter(d -> "Calle Empresa".equals(d.getCalle()) && "789".equals(d.getNumero()))
            .count();
        assertEquals(1, matches);
    }
    
    // 6) Marcar dirección como principal y comprobar que se guarda correctamente.
    @Test
    void marcarDireccionComoPrincipal_GuardaCorrectamente() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(user, "PERSONAL", "Calle Principal", "1", true);

        var resp = direccionService.registrarDireccion(dto);
        // respuesta no nula y campos OK
        assertTrue(resp != null);
        assertEquals("Calle Principal", resp.getCalle());
        assertTrue(Boolean.TRUE.equals(resp.getEsPrincipal()));

        // verificar en repo
        var listas = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario());
        assertFalse(listas.isEmpty());

        // exactamente UNA principal para el usuario
        long principals = listas.stream().filter(d -> Boolean.TRUE.equals(d.getEsPrincipal())).count();
        assertEquals(1, principals);

        // comprobar que la dirección creada coincide con lo enviado
        Direccion saved = listas.stream()
            .filter(d -> "Calle Principal".equals(d.getCalle()) && "1".equals(d.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró la dirección creada"));
        assertTrue(Boolean.TRUE.equals(saved.getEsPrincipal()));
    }
    
    // 7) Cambiar principal: nueva principal desmarca la anterior.
    @Test
    void cambiarDireccionPrincipal_NuevaDesmarcaAnterior() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // Crear primera dirección como principal
        RegistroDireccionDTO first = crearDireccionParaUsuario(user, "PERSONAL", "Calle A", "1", true);
        var resp1 = direccionService.registrarDireccion(first);
        assertTrue(resp1 != null);

        // Comprobar que existe 1 principal (la primera)
        var listas1 = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario());
        long principals1 = listas1.stream().filter(d -> Boolean.TRUE.equals(d.getEsPrincipal())).count();
        assertEquals(1, principals1);
        Direccion savedFirst = listas1.stream()
            .filter(d -> "Calle A".equals(d.getCalle()) && "1".equals(d.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró la primera dirección principal"));

        assertTrue(Boolean.TRUE.equals(savedFirst.getEsPrincipal()));

        // Crear segunda dirección también marcada como principal -> debe desmarcar la anterior
        RegistroDireccionDTO second = crearDireccionParaUsuario(user, "PERSONAL", "Calle B", "2", true);
        var resp2 = direccionService.registrarDireccion(second);
        assertTrue(resp2 != null);

        // Verificar en repo: exactamente 1 principal y que es la segunda
        var listas2 = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario());
        long principals2 = listas2.stream().filter(d -> Boolean.TRUE.equals(d.getEsPrincipal())).count();
        assertEquals(1, principals2);

        Direccion savedSecond = listas2.stream()
            .filter(d -> "Calle B".equals(d.getCalle()) && "2".equals(d.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró la segunda dirección creada"));

        assertTrue(Boolean.TRUE.equals(savedSecond.getEsPrincipal()));

        // Asegurar que la primera ahora NO es principal
        Direccion reloadedFirst = listas2.stream()
            .filter(d -> "Calle A".equals(d.getCalle()) && "1".equals(d.getNumero()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró la primera dirección después del cambio"));
        assertTrue(Boolean.FALSE.equals(reloadedFirst.getEsPrincipal()));
    }
    
    // 8) Activar/desactivar dirección y comprobar efectos en consultas.
    @Test
    void activarDesactivarDireccion_EfectosEnConsultas() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y registrar dirección (vía service para probar la lógica de negocio)
        RegistroDireccionDTO dto = crearDireccionParaUsuario(user, "PERSONAL", "Calle Secundaria", "1", true);
        var resp = direccionService.registrarDireccion(dto);
        Long id = resp.getIdDireccion();

        // comprobar que la dirección creada aparece en el repo general
        var all = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario());
        assertTrue(all.stream().anyMatch(d -> id.equals(d.getIdDireccion())));

        // desactivar la dirección (simula actualización del service)
        Direccion direccion = direccionRepository.findById(id).orElseThrow();
        direccion.setActiva(false);
        direccionRepository.save(direccion);

        // comprobar que no aparece en el filtrado de activas
        var activas = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario())
            .stream()
            .filter(Direccion::getActiva)
            .toList();
        assertFalse(activas.stream().anyMatch(d -> id.equals(d.getIdDireccion())));
    }
    
    // 9) Actualizar dirección (datos y ubicación) y verificar persistencia.
    @Test
    void actualizarDireccion_DatosYUbicacion_Persisten() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();
        // crear y registrar dirección inicial
        RegistroDireccionDTO dto = crearDireccionParaUsuario(user, "PERSONAL", "Calle Update", "10", false);
        var resp = direccionService.registrarDireccion(dto);
        Long id = resp.getIdDireccion();

        // comprobar valores iniciales
        Direccion before = direccionRepository.findById(id).orElseThrow();
        assertEquals("Calle Update", before.getCalle());
        assertEquals("10", before.getNumero());

        // aplicar cambios (simula actualización a través del service/repo)
        // cambiamos datos y flags; si hubiera un método de actualización en el service, preferible usarlo
        before.setCalle("Calle Updated");
        before.setNumero("99");
        before.setActiva(false);
        direccionRepository.save(before);

        // recargar y verificar persistencia y que no se creó otro registro
        Direccion updated = direccionRepository.findById(id).orElseThrow();
        assertEquals(id, updated.getIdDireccion());
        assertEquals("Calle Updated", updated.getCalle());
        assertEquals("99", updated.getNumero());
        assertFalse(updated.getActiva());

        long total = direccionRepository.count();
        assertTrue(total >= 1);
    }
    
    // 10) Crear/actualizar con IDs de ubicación inválidos -> lanzar error.
    @Test
    void crearActualizar_Direccion_UbicacionInvalida_Error() throws Exception {
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(-1L);
        dto.setIdProvincia(-1L);
        dto.setIdDepartamento(-1L);
        dto.setIdMunicipio(-1L);
        dto.setIdLocalidad(-1L);
        dto.setTipo("PERSONAL");
        dto.setCalle("Calle Bad");
        dto.setNumero("0");
        dto.setIdUsuario(direccionDTO.getIdUsuario());

        MvcResult res = registrarDireccion(dto);
        int status = res.getResponse().getStatus();
        // esperamos rechazo por datos de ubicación inválidos (4xx)
        assertTrue(status >= 400 && status < 500);
    }

    // 11) Crear/actualizar con usuario/perfil empresa inexistente -> lanzar error.
    @Test
    void crearActualizar_Direccion_OwnerInexistente_Error() throws Exception {
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setTipo("PERSONAL");
        dto.setCalle("Calle NoOwner");
        dto.setNumero("1");
        dto.setIdUsuario(9999999L); // usuario inexistente

        MvcResult res = registrarDireccion(dto);
        int status = res.getResponse().getStatus();
        assertTrue(status >= 400 && status < 500);
    }

    // 12) Obtener dirección por ID con validación de owner (usuario o empresa).
    @Test
    void obtenerDireccionPorId_ValidarOwner() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();
        RegistroDireccionDTO dto = crearDireccionParaUsuario(user, "PERSONAL", "Calle Verif", "7", false);
        var resp = direccionService.registrarDireccion(dto);
        Long id = resp.getIdDireccion();

        Direccion dir = direccionRepository.findById(id).orElseThrow();
        // validar owner
        assertEquals(user.getIdUsuario(), dir.getUsuario().getIdUsuario());
    }

    // 13) Listar direcciones por usuario con filtros (esPrincipal, activa).
    @Test
    void listarDireccionesPorUsuario_FiltrosPrincipalActiva() throws Exception {
        Usuario user = usuarioRepository.findByUsername("usuario123").orElseThrow();

        registrarDireccionExpectOk(crearDireccionParaUsuario(user, "PERSONAL", "Filtro 1", "1", true));
        registrarDireccionExpectOk(crearDireccionParaUsuario(user, "PERSONAL", "Filtro 2", "2", false));

        var listas = direccionRepository.findByUsuario_IdUsuario(user.getIdUsuario());

        var principales = listas.stream().filter(Direccion::getEsPrincipal).toList();
        assertTrue(principales.size() >= 1);

        var activas = listas.stream().filter(Direccion::getActiva).toList();
        assertTrue(activas.size() >= 1);
    }

    // 14) Listar direcciones por empresa con filtros (esPrincipal, activa).
    @Test
    void listarDireccionesPorEmpresa_FiltrosPrincipalActiva() throws Exception {
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
        perfilEmpresaRepository.save(empresa);

        PerfilEmpresa empresa2 = new PerfilEmpresa();
        empresa2.setRazonSocial("EmpresaTest2");
        empresa2.setCuit("12987654321");
        empresa2.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa2.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa2.setEmailEmpresa("correoempresa1@noenviar.com");
        empresa2.setRequiereFacturacion(true);
        empresa2.setFechaCreacion(LocalDateTime.now());
        empresa2.setFechaUltimaModificacion(LocalDateTime.now());
        empresa2.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa2);

        // dirección para empresa 1 (principal, activa)
        RegistroDireccionDTO dto = new RegistroDireccionDTO();
        dto.setIdPais(direccionDTO.getIdPais());
        dto.setIdProvincia(direccionDTO.getIdProvincia());
        dto.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        dto.setTipo("FISCAL");
        dto.setCalle("Emp Calle 1");
        dto.setNumero("1");
        dto.setActiva(true);
        dto.setEsPrincipal(true);
        dto.setUsarComoEnvio(true);
        dto.setCodigoPostal("5000");
        dto.setIdUsuario(null);
        registrarDireccionExpectOk(dto);

        // dirección para empresa 2 (no principal, activa)
        RegistroDireccionDTO dto2 = new RegistroDireccionDTO();
        dto2.setIdPais(direccionDTO.getIdPais());
        dto2.setIdProvincia(direccionDTO.getIdProvincia());
        dto2.setIdDepartamento(direccionDTO.getIdDepartamento());
        dto2.setIdMunicipio(direccionDTO.getIdMunicipio());
        dto2.setIdLocalidad(direccionDTO.getIdLocalidad());
        dto2.setIdPerfilEmpresa(empresa2.getIdPerfilEmpresa()); // <-- corregido
        dto2.setTipo("FISCAL");
        dto2.setCalle("Emp Calle 2");
        dto2.setNumero("2");
        dto2.setActiva(true);
        dto2.setEsPrincipal(false);
        dto2.setUsarComoEnvio(true);
        dto2.setCodigoPostal("6000");
        dto2.setIdUsuario(null);
        registrarDireccionExpectOk(dto2);

        // verificar por empresa 1
        var listas1 = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertTrue(listas1.size() >= 1);
        var principales1 = listas1.stream().filter(Direccion::getEsPrincipal).toList();
        assertEquals(1, principales1.size()); // sólo 1 principal para empresa 1
        var activas1 = listas1.stream().filter(Direccion::getActiva).toList();
        assertTrue(activas1.size() >= 1);

        // verificar por empresa 2
        var listas2 = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa2.getIdPerfilEmpresa());
        assertTrue(listas2.size() >= 1);
        var principales2 = listas2.stream().filter(Direccion::getEsPrincipal).toList();
        assertEquals(0, principales2.size()); // no principal en empresa2 según lo registrado
        var activas2 = listas2.stream().filter(Direccion::getActiva).toList();
        assertTrue(activas2.size() >= 1);
    }
    
    // 15) Operaciones en cascada: eliminar usuario/perfil y comprobar efecto según política.
    @Test
    void cascadaEliminar_Owner_ComportamientoCorrecto() throws Exception {

        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        
        // Registrar una dirección asociada al usuario
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Calle Cascade", "1", true);
        registrarDireccionExpectOk(dto);
        
        // Eliminar usuario y comprobar efecto en direcciones
        usuarioRepository.delete(usuario);

        var listas = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(listas.isEmpty());
    }

    // 16) Caso E2E service->repo: registrar dirección por service y verificar en repo.
    @Test
    void e2e_RegistrarDireccion_ServiceToRepo_VerificarRepo() throws Exception {

        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroDireccionDTO dto1 = crearDireccionParaUsuario(usuario, "PERSONAL", "Calle Cascade", "1", true);
        var resp = direccionService.registrarDireccion(dto1);

        Long id = resp.getIdDireccion();
        assertTrue(direccionRepository.findById(id).isPresent());
    }
    
}
