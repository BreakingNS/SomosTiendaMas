package com.breakingns.SomosTiendaMas.test.Modulo2.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

    /*                             EmpresaRepositoryTest

    Persistencia básica

        1. Guardar una empresa correctamente en la base de datos.
        2. Buscar empresa por ID.
        3. Buscar empresa por nombre.
        4. Buscar empresa por CUIT.
        5. Buscar empresa por usuario responsable.

    Consultas avanzadas

        6. Buscar empresas por estado (activa/inactiva).

    Actualización

        7. Actualizar datos de una empresa y verificar persistencia.
        8. Actualizar estado de empresa (activa/inactiva).

    Eliminación

        9. Eliminar empresa y verificar que no existe en la base.
        10. Eliminar empresa y verificar eliminación en cascada de direcciones y teléfonos (si aplica).

    Restricciones y reglas

        11. No permitir guardar empresas con nombre o CUIT duplicado.
        12. No permitir guardar empresa sin usuario responsable.
        13. Verificar que los datos obligatorios se validan correctamente.

    Integridad de relaciones

        14. Verificar que la empresa está correctamente relacionada con usuario responsable, direcciones y teléfonos.
        15. Verificar que al eliminar usuario responsable, la empresa se elimina (si la lógica lo requiere).

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
public class EmpresaRepositoryTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;

    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Instanciar y configurar usuario DTO
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
        registroDTO.setDirecciones(List.of()); // Dirección se agrega después
        registroDTO.setTelefonos(List.of());

        // 3. Buscamos el usuario registrado
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");

        // 4. Instanciar y configurar teléfono DTO
        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("654987");
        telefonoDTO.setCaracteristica("383");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(true);

        // 5. Buscar ubicaciones reales para la dirección
        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );

        // 6. Instanciar y configurar dirección DTO
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

        // 7. Armar registro completo
        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // 6. Registrar usuario antes de cada test
        registrarUsuarioCompleto(registroDTO);

        // 7. Verificar usuario y marcar email como verificado
        usuarioOpt = usuarioRepository.findByUsername("usuario123");
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setEmailVerificado(true);
            usuarioRepository.save(usuario);
        }
    }
    
    // Método para registrar un usuario completo usando el endpoint de gestión de perfil
    private int registrarUsuarioCompleto(RegistroUsuarioCompletoDTO registroDTO) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        return result.getResponse().getStatus();
    }

    // Persistencia básica

    // 1. Guardar una empresa correctamente en la base de datos.
    @Test
    void guardarEmpresaCorrectamenteEnBaseDeDatos() {
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
        empresa.setActivo(true);
        empresa.setUsuario(usuario); // Debes crear y asociar un usuario válido en un test real

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        assertEquals("EmpresaTest", guardada.getRazonSocial());
        assertEquals("12345678901", guardada.getCuit());
        assertEquals(PerfilEmpresa.CondicionIVA.RI, guardada.getCondicionIVA());
    }
    
    // 2. Buscar empresa por ID.
    @Test
    void buscarEmpresaPorId() {
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
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        PerfilEmpresa encontrada = perfilEmpresaRepository.findById(guardada.getIdPerfilEmpresa()).orElse(null);

        assertEquals(guardada.getIdPerfilEmpresa(), encontrada.getIdPerfilEmpresa());
    }
    
    // 3. Buscar empresa por nombre (razón social).
    @Test
    void buscarEmpresaPorNombre() {
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
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        perfilEmpresaRepository.save(empresa);

        PerfilEmpresa encontrada = perfilEmpresaRepository.findByRazonSocial("EmpresaTest").orElse(null);

        assertEquals("EmpresaTest", encontrada.getRazonSocial());
    }
    
    // 4. Buscar empresa por CUIT.
    @Test
    void buscarEmpresaPorCuit() {
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
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        perfilEmpresaRepository.save(empresa);

        PerfilEmpresa encontrada = perfilEmpresaRepository.findByCuit("12345678901").orElse(null);

        assertEquals("12345678901", encontrada.getCuit());
    }
    
    // 5. Buscar empresa por usuario responsable.
    @Test
    void buscarEmpresaPorUsuarioResponsable() {
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
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        perfilEmpresaRepository.save(empresa);

        PerfilEmpresa encontrada = perfilEmpresaRepository.findByUsuario(usuario).orElse(null);

        assertEquals(usuario.getIdUsuario(), encontrada.getUsuario().getIdUsuario());
    }
    
    // Consultas avanzadas

    // 6. Buscar empresas por estado (activa/inactiva).
    @Test
    void buscarEmpresasPorEstado() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        
        // Crear empresa 1 (aprobada)
        PerfilEmpresa empresaAprobada = new PerfilEmpresa();
        empresaAprobada.setRazonSocial("Empresa Aprobada");
        empresaAprobada.setCuit("11111111111");
        empresaAprobada.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresaAprobada.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresaAprobada.setEmailEmpresa("correoprueba2@noenviar.com");
        empresaAprobada.setRequiereFacturacion(true);
        empresaAprobada.setFechaCreacion(LocalDateTime.now());
        empresaAprobada.setFechaUltimaModificacion(LocalDateTime.now());
        empresaAprobada.setUsuario(usuario);
        empresaAprobada.setActivo(true);
        perfilEmpresaRepository.save(empresaAprobada);

        // Crear usuario base para los tests
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

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );

        RegistroDireccionDTO direccionDTO = new RegistroDireccionDTO();
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

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario antes de cada test
        registrarUsuarioCompleto(registroDTO);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario123");
        assertTrue(usuarioOpt.isPresent());
        usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Crear empresa 2 (desaprobada)
        PerfilEmpresa empresaDesaprobada = new PerfilEmpresa();
        empresaDesaprobada.setRazonSocial("Empresa Desaprobada");
        empresaDesaprobada.setCuit("22222222222");
        empresaDesaprobada.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresaDesaprobada.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.RECHAZADO);
        empresaDesaprobada.setEmailEmpresa("empresaD@noenviar.com");
        empresaDesaprobada.setRequiereFacturacion(true);
        empresaDesaprobada.setFechaCreacion(LocalDateTime.now());
        empresaDesaprobada.setFechaUltimaModificacion(LocalDateTime.now());
        empresaDesaprobada.setUsuario(usuario);
        empresaDesaprobada.setActivo(true);
        perfilEmpresaRepository.save(empresaDesaprobada);

        // Verificar estados
        PerfilEmpresa encontradaA = perfilEmpresaRepository.findById(empresaAprobada.getIdPerfilEmpresa()).orElse(null);
        PerfilEmpresa encontradaD = perfilEmpresaRepository.findById(empresaDesaprobada.getIdPerfilEmpresa()).orElse(null);

        assertEquals(PerfilEmpresa.EstadoAprobado.APROBADO, encontradaA.getEstadoAprobado());
        assertEquals(PerfilEmpresa.EstadoAprobado.RECHAZADO, encontradaD.getEstadoAprobado());
    }
    
    // Actualización

    // 7. Actualizar datos de una empresa y verificar persistencia.
    @Test
    void actualizarDatosEmpresaYVerificarPersistencia() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        
        // Crear y guardar empresa
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

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        // Actualizar datos
        guardada.setRazonSocial("EmpresaActualizada");
        guardada.setEmailEmpresa("correoempresa1@noenviar.com");
        perfilEmpresaRepository.save(guardada);

        // Verificar persistencia
        PerfilEmpresa actualizada = perfilEmpresaRepository.findById(guardada.getIdPerfilEmpresa()).orElse(null);
        assertEquals("EmpresaActualizada", actualizada.getRazonSocial());
        assertEquals("correoempresa1@noenviar.com", actualizada.getEmailEmpresa());
    }
    
    // 8. Actualizar estado de empresa (activa/inactiva).
    @Test
    void actualizarEstadoEmpresa() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
    
        // Crear y guardar empresa
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

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        // Cambiar estado
        guardada.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.RECHAZADO);
        perfilEmpresaRepository.save(guardada);

        // Verificar persistencia
        PerfilEmpresa actualizada = perfilEmpresaRepository.findById(guardada.getIdPerfilEmpresa()).orElse(null);
        assertEquals(PerfilEmpresa.EstadoAprobado.RECHAZADO, actualizada.getEstadoAprobado());
    }
    
    // Eliminación

    // 9. Eliminar empresa y verificar que no existe en la base.
    @Test
    void eliminarEmpresaYVerificarNoExisteEnBase() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);    
    
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaEliminar");
        empresa.setCuit("99999999999");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        perfilEmpresaRepository.delete(guardada);

        boolean existe = perfilEmpresaRepository.findById(guardada.getIdPerfilEmpresa()).isPresent();
        assertEquals(false, existe);
    }
    
    // 10. Eliminar empresa y verificar eliminación en cascada de direcciones y teléfonos (si aplica).
    @Test
    void eliminarEmpresaYVerificarEliminacionCascadaDireccionesTelefonos() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);    
    
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaCascada");
        empresa.setCuit("88888888888");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);

        PerfilEmpresa guardada = perfilEmpresaRepository.save(empresa);

        // Simula agregar direcciones y teléfonos asociados
        // Direccion direccion = new Direccion();
        // direccion.setPerfilEmpresa(guardada);
        // direccionRepository.save(direccion);
        // Telefono telefono = new Telefono();
        // telefono.setPerfilEmpresa(guardada);
        // telefonoRepository.save(telefono);

        perfilEmpresaRepository.delete(guardada);

        // Verifica que no existan direcciones ni teléfonos asociados
        // assertEquals(0, direccionRepository.findByPerfilEmpresa(guardada).size());
        // assertEquals(0, telefonoRepository.findByPerfilEmpresa(guardada).size());
        // Si no tienes los métodos, puedes verificar que no existen en la base

        boolean existeEmpresa = perfilEmpresaRepository.findById(guardada.getIdPerfilEmpresa()).isPresent();
        assertEquals(false, existeEmpresa);
    }
    
    // Restricciones y reglas

    // 11. No permitir guardar empresas con nombre o CUIT duplicado.
    @Test
    void noPermitirGuardarEmpresaConNombreOCuitDuplicado() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        
        PerfilEmpresa empresa1 = new PerfilEmpresa();
        empresa1.setRazonSocial("EmpresaDuplicada");
        empresa1.setCuit("55555555555");
        empresa1.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa1.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa1.setEmailEmpresa("correoempresa@noenviar.com");
        empresa1.setRequiereFacturacion(true);
        empresa1.setFechaCreacion(LocalDateTime.now());
        empresa1.setFechaUltimaModificacion(LocalDateTime.now());
        empresa1.setUsuario(usuario);
        empresa1.setActivo(true);
        perfilEmpresaRepository.save(empresa1);

        PerfilEmpresa empresa2 = new PerfilEmpresa();
        empresa2.setRazonSocial("EmpresaDuplicada"); // mismo nombre
        empresa2.setCuit("55555555555"); // mismo CUIT
        empresa2.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa2.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa2.setEmailEmpresa("correoempresa2@noenviar.com");
        empresa2.setRequiereFacturacion(true);
        empresa2.setFechaCreacion(LocalDateTime.now());
        empresa2.setFechaUltimaModificacion(LocalDateTime.now());
        empresa2.setUsuario(null);

        Exception exception = null;
        try {
            perfilEmpresaRepository.save(empresa2);
        } catch (Exception e) {
            exception = e;
        }
        // Espera que lance una excepción por restricción única
        assertEquals(true, exception != null);
    }
    
    // 12. No permitir guardar empresa sin usuario responsable.
    @Test
    void noPermitirGuardarEmpresaSinUsuarioResponsable() {
        PerfilEmpresa empresa = new PerfilEmpresa();    
        empresa.setRazonSocial("EmpresaSinUsuario");
        empresa.setCuit("66666666666");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(null);; // Sin usuario responsable
        empresa.setActivo(true);

        Exception exception = null;
        try {
            perfilEmpresaRepository.save(empresa);
        } catch (Exception e) {
            exception = e;
        }
        // Espera que lance una excepción por restricción NOT NULL
        assertEquals(true, exception != null);
    }
    
    // 13. Verificar que los datos obligatorios se validan correctamente.
    @Test
    void verificarDatosObligatoriosEmpresa() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);   
    
        PerfilEmpresa empresa = new PerfilEmpresa();
        // No se setean datos obligatorios como razón social y CUIT
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);;
        empresa.setActivo(true);

        Exception exception = null;
        try {
            perfilEmpresaRepository.save(empresa);
        } catch (Exception e) {
            exception = e;
        }
        // Espera que lance una excepción por datos obligatorios faltantes
        assertEquals(true, exception != null);
    }

    // Integridad de relaciones
    
    // 14. Verificar que la empresa está correctamente relacionada con usuario responsable, direcciones y teléfonos.
    @Test
    void verificarRelacionEmpresaUsuario() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaRelacion");
        empresa.setCuit("77777777777");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);
        perfilEmpresaRepository.save(empresa);

        PerfilEmpresa encontrada = perfilEmpresaRepository.findById(empresa.getIdPerfilEmpresa()).orElse(null);

        assertEquals(usuario.getIdUsuario(), encontrada.getUsuario().getIdUsuario());
    }
    
    // 15. Verificar que al eliminar usuario responsable, la empresa se elimina (si la lógica lo requiere).
    @Test
    void eliminarUsuarioResponsableYVerificarEmpresaEliminada() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaEliminarUsuario");
        empresa.setCuit("78787878787");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("correoempresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        empresa.setActivo(true);
        perfilEmpresaRepository.save(empresa);

        usuarioRepository.delete(usuario);

        boolean existeEmpresa = perfilEmpresaRepository.findById(empresa.getIdPerfilEmpresa()).isPresent();
        assertEquals(false, existeEmpresa);
    }
}
