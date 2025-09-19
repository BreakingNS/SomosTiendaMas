package com.breakingns.SomosTiendaMas.test.Modulo2.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*                                                  TelefonoRepositoryTest

    Persistencia básica

        1. Guardar un teléfono correctamente en la base de datos.
        2. Buscar teléfono por ID.
        3. Listar teléfonos por usuario.
        4. Listar teléfonos por perfil empresa.
        5. Actualizar datos de un teléfono y verificar persistencia.
        6. Eliminar teléfono y verificar que no existe en la base.

    Eliminación en cascada

        7. Eliminar usuario y verificar que se eliminan sus teléfonos asociados.
        8. Eliminar perfil empresa y verificar que se eliminan sus teléfonos asociados.

    Restricciones y reglas

        9. No permitir guardar teléfono sin datos obligatorios (tipo, número, característica).
        10. Aplicar valores por defecto si vienen null (activo=true, verificado=false).

    Duplicados y relaciones

        11. No permitir guardar teléfono duplicado para el mismo owner (reusar o rechazar).
        12. Permitir mismo número para distinto owner (usuario distinto / empresa distinta).
        13. Asociar teléfono copiado a otro usuario/empresa crea fila independiente (si owner distinto).

    Consultas y filtros

        14. Buscar teléfonos activos/inactivos por usuario.
        15. Buscar teléfonos verificados/no verificados por usuario.
        16. Buscar teléfonos por tipo (PRINCIPAL, SECUNDARIO, WHATSAPP, EMPRESA).

    Integración controller/service

        17. Registrar teléfono vía endpoint público y validar status/body.
        18. Registrar múltiples teléfonos en registro inicial (si aplica).
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
public class TelefonoRepositoryTest {
    
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
    private final ITelefonoRepository telefonoRepository;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;

    @BeforeEach
    void setUp() throws Exception {
        
        // 0. Importar ubicaciones para direcciones
        mockMvc.perform(post("/api/import-codigos-area/excel")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andReturn();

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

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        // 2. Armar el registro completo con las listas ya instanciadas
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of()); // no registramos direcciones aquí para tests de telefono
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // 3. Registrar usuario antes de ejecutar tests
        int status = registrarUsuarioCompleto(registroDTO);
        assertEquals(200, status, "Registro de usuario en setUp debe devolver 200");

        // 4. Verificar usuario y marcar email verificado para permitir operaciones
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // nota: no registramos direcciones en el setUp para estos tests de telefono
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

    private void importarUbicacionesMock() throws Exception {
        mockMvc.perform(post("/api/import-ubicaciones/provincias")).andReturn();
        mockMvc.perform(post("/api/import-ubicaciones/departamentos")
            .param("provincia", "CATAMARCA")).andReturn();
        mockMvc.perform(post("/api/import-ubicaciones/localidades")
            .param("departamento", "CAPITAL")).andReturn();
        mockMvc.perform(post("/api/import-ubicaciones/municipios")
            .param("localidad", "SAN FERNANDO DEL VALLE DE CATAMARCA")).andReturn();
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

    // helper único: hace POST y devuelve MvcResult para inspección en tests (telefonos)
    private MvcResult registrarTelefono(RegistroTelefonoDTO telefonoDTO) throws Exception {
        return mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(telefonoDTO)))
            .andReturn();
    }

    private MvcResult registrarTelefonoExpectOk(RegistroTelefonoDTO telefonoDTO) throws Exception {
        return mockMvc.perform(post("/api/telefono/public")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(telefonoDTO)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andReturn();
    }

    // helper para construir DTOs de telefono vinculados a usuario o empresa
    private RegistroTelefonoDTO crearTelefonoParaUsuario(Usuario usuario, String tipo, String numero, String caracteristica, Boolean activo, Boolean verificado) {
        RegistroTelefonoDTO t = new RegistroTelefonoDTO();
        t.setTipo(tipo);
        t.setNumero(numero);
        t.setCaracteristica(caracteristica);
        t.setActivo(activo);
        t.setVerificado(verificado);
        t.setIdUsuario(usuario != null ? usuario.getIdUsuario() : null);
        t.setIdPerfilEmpresa(null);
        return t;
    }

    // helper para construir DTO de telefono vinculado a empresa (idUsuario = null)
    private RegistroTelefonoDTO crearTelefonoParaEmpresa(PerfilEmpresa empresa, String tipo, String numero, String caracteristica, Boolean activo, Boolean verificado) {
        RegistroTelefonoDTO t = new RegistroTelefonoDTO();
        t.setTipo(tipo);
        t.setNumero(numero);
        t.setCaracteristica(caracteristica);
        t.setActivo(activo);
        t.setVerificado(verificado);
        t.setIdUsuario(null);
        t.setIdPerfilEmpresa(empresa != null ? empresa.getIdPerfilEmpresa() : null);
        return t;
    }

    // Persistencia básica

    // 1. Guardar un teléfono correctamente en la base de datos.
    @Test
    void guardarTelefonoCorrectamente() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(ts.stream().anyMatch(t -> "1122334455".equals(t.getNumero())), "Debe guardarse el teléfono para el usuario");
    }

    // 2. Buscar teléfono por ID.
    @Test
    void buscarTelefonoPorId() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        Telefono tel = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
            .stream().filter(t -> "1122334455".equals(t.getNumero())).findFirst().orElseThrow();

        Optional<Telefono> byId = telefonoRepository.findById(tel.getIdTelefono());
        assertTrue(byId.isPresent(), "Debe encontrarse el teléfono por ID");
        assertEquals("1122334455", byId.get().getNumero());
    }

    // 3. Listar teléfonos por usuario.
    @Test
    void listarTelefonosPorUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1100000001", "11", true, false)).getResponse().getStatus());
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuario, "WHATSAPP", "1100000002", "11", true, false)).getResponse().getStatus());

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long count = ts.stream().filter(t -> t.getUsuario() != null && t.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())).count();
        assertTrue(count >= 2, "Debe listar los teléfonos del usuario");
    }

    // 4. Listar teléfonos por perfil empresa.
    @Test
    void listarTelefonosPorPerfilEmpresa() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTel");
        empresa.setCuit("20900000001");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresaTel@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa);

        assertEquals(200, registrarTelefono(crearTelefonoParaEmpresa(empresa, "EMPRESA", "1144444444", "11", true, false)).getResponse().getStatus());
        List<Telefono> ts = telefonoRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertTrue(ts.stream().anyMatch(t -> "1144444444".equals(t.getNumero())), "Debe listar los teléfonos de la empresa");
    }

    // 5. Actualizar datos de un teléfono y verificar persistencia.
    @Test
    void actualizarTelefonoYVerificarPersistencia() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1166666666", "11", true, false)).getResponse().getStatus());

        Telefono tel = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
            .stream().filter(t -> "1166666666".equals(t.getNumero())).findFirst().orElseThrow();

        tel.setNumero("1177777777");
        tel.setCaracteristica("221");
        tel.setActivo(false);
        tel.setVerificado(true);
        telefonoRepository.save(tel);

        Telefono reloaded = telefonoRepository.findById(tel.getIdTelefono()).orElseThrow();
        assertEquals("1177777777", reloaded.getNumero());
        assertEquals("221", reloaded.getCaracteristica());
        assertEquals(false, reloaded.getActivo());
        assertEquals(true, reloaded.getVerificado());
    }

    // 6. Eliminar teléfono y verificar que no existe en la base.
    @Test
    void eliminarTelefonoYVerificarNoExisteEnBase() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1133333333", "11", true, false)).getResponse().getStatus());

        Telefono tel = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
            .stream().filter(t -> "1133333333".equals(t.getNumero())).findFirst().orElseThrow();

        Long id = tel.getIdTelefono();
        telefonoRepository.deleteById(id);
        assertTrue(telefonoRepository.findById(id).isEmpty(), "El teléfono no fue eliminado");
    }

    // Eliminación en cascada

    // 7. Eliminar usuario y verificar que se eliminan sus teléfonos asociados.
    @Test
    void eliminarUsuarioYVerificarEliminacionCascadaTelefonos() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1111111111", "11", true, false));
        registrarTelefono(crearTelefonoParaUsuario(usuario, "SECUNDARIO", "1222222222", "11", true, false));

        usuarioRepository.deleteById(usuario.getIdUsuario());
        List<Telefono> despues = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(despues.isEmpty(), "Los teléfonos del usuario deberían eliminarse al borrar el usuario");
    }

    // 8. Eliminar perfil empresa y verificar que se eliminan sus teléfonos asociados.
    @Test
    void eliminarPerfilEmpresaYVerificarEliminacionCascadaTelefonos() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaCascade");
        empresa.setCuit("20900000002");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresaCascade@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa);

        registrarTelefono(crearTelefonoParaEmpresa(empresa, "EMPRESA", "1155555555", "11", true, false));

        perfilEmpresaRepository.deleteById(empresa.getIdPerfilEmpresa());
        List<Telefono> despues = telefonoRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertTrue(despues.isEmpty(), "Los teléfonos de la empresa deberían eliminarse al borrar el perfil");
    }

    // Restricciones y reglas

    // 9. No permitir guardar teléfono sin datos obligatorios (tipo, número, característica).
    @Test
    void noPermitirGuardarTelefonoSinObligatorios() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearTelefonoParaUsuario(usuario, null, null, null, true, false);
        MvcResult r = registrarTelefono(dto);
        int status = r.getResponse().getStatus();
        assertTrue(status >= 400, "Debería fallar por faltar datos obligatorios (status=" + status + ")");
    }

    // 9b. No permitir guardar teléfono con característica inválida (no existe en codigos_area).
    @Test
    void noPermitirGuardarTelefonoConCaracteristicaInvalida() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1122334455", "9999", true, false); // "9999" no existe
        MvcResult r = registrarTelefono(dto);
        int status = r.getResponse().getStatus();
        assertTrue(status >= 400, "Debe fallar si la característica no existe en la tabla (status=" + status + ")");
    }

    // 10. Aplicar valores por defecto si vienen null (activo=true, verificado=false).
    @Test
    void aplicarValoresPorDefecto() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1190000000", "11", null, null);
        assertEquals(200, registrarTelefono(dto).getResponse().getStatus());

        Telefono tel = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
            .stream().filter(t -> "1190000000".equals(t.getNumero())).findFirst().orElseThrow();

        assertEquals(true, tel.getActivo(), "Activo debería ser true por defecto");
        assertEquals(false, tel.getVerificado(), "Verificado debería ser false por defecto");
    }

    // Duplicados y relaciones

    // 11. No permitir guardar teléfono duplicado para el mismo owner (reusar o rechazar).
    @Test
    void noPermitirTelefonoDuplicadoParaMismoOwner() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto1 = crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1188888888", "11", true, false);
        MvcResult r1 = registrarTelefono(dto1);
        assertEquals(200, r1.getResponse().getStatus());

        RegistroTelefonoDTO dto2 = crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1188888888", "11", true, false);
        MvcResult r2 = registrarTelefono(dto2);
        int status2 = r2.getResponse().getStatus();

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long iguales = ts.stream()
            .filter(t -> "1188888888".equals(t.getNumero()) && "11".equals(t.getCaracteristica()))
            .count();

        assertTrue(status2 >= 400 || iguales == 1, "No debe haber duplicados para el mismo owner (status=" + status2 + ", iguales=" + iguales + ")");
    }

    // 12. Permitir mismo número para distinto owner (usuario distinto / empresa distinta).
    @Test
    void permitirMismoNumeroParaOwnerDistinto() throws Exception {
        // Usuario A
        Usuario usuarioA = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // Usuario B
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioB");
        usuarioDTO.setEmail("usuarioB@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Maria");
        usuarioDTO.setApellidoResponsable("Lopez");
        usuarioDTO.setDocumentoResponsable("99999999");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1991, 2, 2));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO regB = new RegistroUsuarioCompletoDTO();
        regB.setUsuario(usuarioDTO);
        regB.setDirecciones(List.of());
        regB.setTelefonos(List.of());
        assertEquals(200, registrarUsuarioCompleto(regB));

        Usuario usuarioB = usuarioRepository.findByUsername("usuarioB").orElseThrow();
        usuarioB.setEmailVerificado(true);
        usuarioRepository.save(usuarioB);

        // mismo número para A y B
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuarioA, "PRINCIPAL", "1170000000", "11", true, false)).getResponse().getStatus());
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuarioB, "PRINCIPAL", "1170000000", "11", true, false)).getResponse().getStatus());

        List<Telefono> tsA = telefonoRepository.findByUsuario_IdUsuario(usuarioA.getIdUsuario());
        List<Telefono> tsB = telefonoRepository.findByUsuario_IdUsuario(usuarioB.getIdUsuario());

        assertTrue(tsA.stream().anyMatch(t -> "1170000000".equals(t.getNumero())));
        assertTrue(tsB.stream().anyMatch(t -> "1170000000".equals(t.getNumero())));
    }

    // 13. Asociar teléfono copiado a otro owner crea fila independiente (si owner distinto).
    @Test
    void copiarTelefonoAOtroUsuarioCreaFilaIndependiente() throws Exception {
        Usuario usuarioA = usuarioRepository.findByUsername("usuario123").orElseThrow();

        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuarioA, "PRINCIPAL", "1160000000", "11", true, false)).getResponse().getStatus());
        Telefono original = telefonoRepository.findByUsuario_IdUsuario(usuarioA.getIdUsuario())
            .stream().filter(t -> "1160000000".equals(t.getNumero())).findFirst().orElseThrow();

        // crear usuario B
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioCopia");
        usuarioDTO.setEmail("usuarioCopia@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Ana");
        usuarioDTO.setApellidoResponsable("Gomez");
        usuarioDTO.setDocumentoResponsable("87654321");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroUsuarioCompletoDTO reg = new RegistroUsuarioCompletoDTO();
        reg.setUsuario(usuarioDTO);
        reg.setDirecciones(List.of());
        reg.setTelefonos(List.of());
        assertEquals(200, registrarUsuarioCompleto(reg));
        Usuario usuarioB = usuarioRepository.findByUsername("usuarioCopia").orElseThrow();
        usuarioB.setEmailVerificado(true);
        usuarioRepository.save(usuarioB);

        // copiar teléfono a B (misma data)
        assertEquals(200, registrarTelefono(crearTelefonoParaUsuario(usuarioB, "PRINCIPAL", "1160000000", "11", true, false)).getResponse().getStatus());

        Telefono copia = telefonoRepository.findByUsuario_IdUsuario(usuarioB.getIdUsuario())
            .stream().filter(t -> "1160000000".equals(t.getNumero())).findFirst().orElseThrow();

        assertNotEquals(original.getIdTelefono(), copia.getIdTelefono(), "La copia debe tener un id distinto");
        assertNotEquals(original.getUsuario().getIdUsuario(), copia.getUsuario().getIdUsuario(), "La copia debe pertenecer a otro usuario");
    }

    // Consultas y filtros

    // 14. Buscar teléfonos activos/inactivos por usuario.
    @Test
    void buscarTelefonosActivosInactivosPorUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1150000001", "11", true, false));
        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1150000002", "11", false, false));

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long activos = ts.stream().filter(t -> Boolean.TRUE.equals(t.getActivo())).count();
        long inactivos = ts.stream().filter(t -> Boolean.FALSE.equals(t.getActivo())).count();

        assertTrue(activos >= 1, "Debe existir al menos un teléfono activo");
        assertTrue(inactivos >= 1, "Debe existir al menos un teléfono inactivo");
    }

    // 15. Buscar teléfonos verificados/no verificados por usuario.
    @Test
    void buscarTelefonosVerificadosNoVerificadosPorUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1151000001", "11", true, true));
        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1151000002", "11", true, false));

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long verificados = ts.stream().filter(t -> Boolean.TRUE.equals(t.getVerificado())).count();
        long noVerificados = ts.stream().filter(t -> Boolean.FALSE.equals(t.getVerificado())).count();

        assertTrue(verificados >= 1, "Debe existir al menos un teléfono verificado");
        assertTrue(noVerificados >= 1, "Debe existir al menos un teléfono no verificado");
    }

    // 16. Buscar teléfonos por tipo (PRINCIPAL, SECUNDARIO, WHATSAPP, EMPRESA).
    @Test
    void buscarTelefonosPorTipo() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        registrarTelefono(crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1152000001", "11", true, false));
        registrarTelefono(crearTelefonoParaUsuario(usuario, "WHATSAPP", "1152000002", "11", true, false));

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        boolean hayPrincipal = ts.stream().anyMatch(t -> {
            String tipo = t.getTipo() != null ? t.getTipo().toString() : null;
            return "PRINCIPAL".equalsIgnoreCase(tipo);
        });
        boolean hayWhatsapp = ts.stream().anyMatch(t -> {
            String tipo = t.getTipo() != null ? t.getTipo().toString() : null;
            return "WHATSAPP".equalsIgnoreCase(tipo);
        });

        assertTrue(hayPrincipal, "Debe existir teléfono tipo PRINCIPAL");
        assertTrue(hayWhatsapp, "Debe existir teléfono tipo WHATSAPP");
    }

    // Integración controller/service

    // 17. Registrar teléfono vía endpoint público y validar status/body.
    @Test
    void registrarTelefonoEndpointYValidar() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroTelefonoDTO dto = crearTelefonoParaUsuario(usuario, "PRINCIPAL", "1153000000", "11", true, false);
        MvcResult r = registrarTelefono(dto);
        assertEquals(200, r.getResponse().getStatus());

        // validar que se creó en BD
        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(ts.stream().anyMatch(t -> "1153000000".equals(t.getNumero())), "El teléfono debe existir en BD");
        // opcional: validar que el body contiene el número (sin asumir estructura exacta)
        String body = r.getResponse().getContentAsString();
        assertTrue(body == null || body.isEmpty() || body.contains("1153000000") || body.contains("numero"),
            "El body debería contener datos del teléfono (número) o estar vacío si no se mapea");
    }

    // 18. Registrar múltiples teléfonos en registro inicial (si aplica).
    @Test
    void registrarMultiplesTelefonosEnRegistroInicial() throws Exception {
        // armar un nuevo usuario con dos teléfonos
        RegistroUsuarioDTO uDto = new RegistroUsuarioDTO();
        uDto.setUsername("usuarioMultiTel");
        uDto.setEmail("multiTel@noenviar.com");
        uDto.setPassword("ClaveSegura123");
        uDto.setNombreResponsable("Pepe");
        uDto.setApellidoResponsable("Argento");
        uDto.setDocumentoResponsable("55667788");
        uDto.setTipoUsuario("PERSONA_FISICA");
        uDto.setAceptaTerminos(true);
        uDto.setAceptaPoliticaPriv(true);
        uDto.setFechaNacimientoResponsable(LocalDate.of(1985, 5, 5));
        uDto.setGeneroResponsable("MASCULINO");
        uDto.setIdioma("es");
        uDto.setTimezone("America/Argentina/Buenos_Aires");
        uDto.setRol("ROLE_USUARIO");

        RegistroTelefonoDTO t1 = new RegistroTelefonoDTO();
        t1.setTipo("PRINCIPAL"); t1.setNumero("1154000001"); t1.setCaracteristica("11"); t1.setActivo(true); t1.setVerificado(false);
        RegistroTelefonoDTO t2 = new RegistroTelefonoDTO();
        t2.setTipo("WHATSAPP"); t2.setNumero("1154000002"); t2.setCaracteristica("11"); t2.setActivo(true); t2.setVerificado(false);

        RegistroUsuarioCompletoDTO reg = new RegistroUsuarioCompletoDTO();
        reg.setUsuario(uDto);
        reg.setDirecciones(List.of());
        reg.setTelefonos(List.of(t1, t2));

        assertEquals(200, registrarUsuarioCompleto(reg));
        Usuario u = usuarioRepository.findByUsername("usuarioMultiTel").orElseThrow();
        u.setEmailVerificado(true);
        usuarioRepository.save(u);

        List<Telefono> ts = telefonoRepository.findByUsuario_IdUsuario(u.getIdUsuario());
        assertTrue(ts.size() >= 2, "El usuario debería tener al menos 2 teléfonos");
        assertTrue(ts.stream().anyMatch(t -> "1154000001".equals(t.getNumero())));
        assertTrue(ts.stream().anyMatch(t -> "1154000002".equals(t.getNumero())));
    }
    
}
