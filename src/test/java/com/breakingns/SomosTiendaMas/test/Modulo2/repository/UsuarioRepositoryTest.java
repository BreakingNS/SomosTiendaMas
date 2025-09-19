package com.breakingns.SomosTiendaMas.test.Modulo2.repository;

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
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
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
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

    /*                             UsuarioRepositoryTest

    Persistencia básica

        1. Guardar un usuario correctamente en la base de datos.
        2. Buscar usuario por ID.
        3. Buscar usuario por username.
        4. Buscar usuario por email.

    Consultas avanzadas

        5. Buscar usuarios por estado (activo/inactivo).
        6. Buscar usuarios por rol (si aplica).

    Actualización

        7. Actualizar datos de un usuario y verificar persistencia.
        8. Actualizar estado de usuario (activo/inactivo).

    Eliminación

        9. Eliminar usuario y verificar que no existe en la base.
        10. Eliminar usuario y verificar eliminación en cascada de direcciones, teléfonos y empresas (si la lógica lo requiere).

    Restricciones y reglas

        11. No permitir guardar usuarios con username o email duplicado.
        12. Verificar que los datos obligatorios se validan correctamente.

    Integridad de relaciones

        13. Verificar que el usuario está correctamente relacionado con empresas, direcciones y teléfonos.
        14. Verificar que al eliminar usuario, se eliminan empresas, direcciones y teléfonos asociados (si la lógica lo requiere).

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
public class UsuarioRepositoryTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;

    @MockBean
    private EmailService emailService;

    private RegistroUsuarioCompletoDTO registroDTO;
    private RegistroDireccionDTO direccionDTO;

    private final UsuarioServiceImpl usuarioService;

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
        direccionDTO.setUsarComoEnvio(true);    
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

    // Helper para crear dirección para usuario
    private RegistroDireccionDTO crearDireccionParaUsuario(Usuario usuario, String tipo, String calle, String numero, boolean esPrincipal) {
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

    // Helper para crear teléfono para usuario
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

    @Test
    void contextoCargaCorrectamente() throws Exception {
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
        direccionDTO.setUsarComoEnvio(true);    
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

    // Persistencia básica

    // 1. Guardar un usuario correctamente en la base de datos.
    @Test
    void guardarUsuarioCorrectamenteEnBaseDeDatos() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
    
        boolean existe = usuarioRepository.findById(usuario.getIdUsuario()).isPresent();
        assertTrue(existe);
    }
    
    // 2. Buscar usuario por ID.
    @Test
    void buscarUsuarioPorId() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
    
        boolean existe = usuarioRepository.findById(usuario.getIdUsuario()).isPresent();
        assertTrue(existe);
    }
    
    // 3. Buscar usuario por username.
    @Test
    void buscarUsuarioPorUsername() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);

        boolean existe = usuarioRepository.findByUsername(usuario.getUsername()).isPresent();
        assertTrue(existe);
    }
     
    // 4. Buscar usuario por email.
    @Test
    void buscarUsuarioPorEmail() {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);

        boolean existe = usuarioRepository.findByEmail(usuario.getEmail()).isPresent();
        assertTrue(existe);
    }
    
    // Consultas avanzadas

    // 5. Buscar usuarios por estado (activo/inactivo).
    @Test
    void buscarUsuariosPorEstado() throws Exception {
        // Crear usuario base para los tests
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuario456");
        usuarioDTO.setEmail("prueba456@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura456");
        usuarioDTO.setNombreResponsable("Maria");
        usuarioDTO.setApellidoResponsable("Gomez");
        usuarioDTO.setDocumentoResponsable("87654321");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1985, 5, 15));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Cordoba");
        usuarioDTO.setRol("ROLE_USUARIO");

        RegistroDireccionDTO direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Avenida Siempre Viva");
        direccionDTO.setNumero("742");
        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Optional<Localidad> localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        );

        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdLocalidad(localidad.get().getId());
        direccionDTO.setIdMunicipio(municipio.getId());

        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1133445566");
        telefonoDTO.setCaracteristica("0353");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(true);

        RegistroUsuarioCompletoDTO registroDTO1 = new RegistroUsuarioCompletoDTO();
        registroDTO1.setUsuario(usuarioDTO);
        registroDTO1.setDirecciones(List.of(direccionDTO));
        registroDTO1.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario antes de cada test
        registrarUsuarioCompleto(registroDTO1);

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario456");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        Usuario usuarioActivo = usuarioRepository.findByUsername("usuario123").orElse(null);
        Usuario usuarioInactivo = usuarioRepository.findByUsername("usuario456").orElse(null);

        List<Usuario> activos = usuarioRepository.findByActivo(true);
        List<Usuario> inactivos = usuarioRepository.findByActivo(false);

        assertTrue(activos.stream().anyMatch(u -> u.getUsername().equals("usuario123")));
        assertTrue(inactivos.stream().anyMatch(u -> u.getUsername().equals("usuario456")));
    }
    
    // 6. Buscar usuarios por rol (si aplica).
    @Test
    void buscarUsuariosPorRol() {
        Usuario admin = new Usuario();
        admin.setUsername("adminTest");
        admin.setPassword("P123456");
        admin.setEmail("correoprueba1@noenviar.com");
        admin.setActivo(true);
        admin.setEmailVerificado(true);
        admin.setFechaRegistro(java.time.LocalDateTime.now());
        admin.setIntentosFallidosLogin(0);
        admin.setCuentaBloqueada(false);
        admin.setTipoUsuario(Usuario.TipoUsuario.PERSONA_FISICA);
        admin.setNombreResponsable("Admin");
        admin.setApellidoResponsable("Test");
        admin.setDocumentoResponsable("99999999");
        admin.setAceptaTerminos(true);
        admin.setAceptaPoliticaPriv(true);
        admin.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        admin.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioService.registrarConRol(admin, RolNombre.ROLE_ADMIN);

        List<Usuario> admins = usuarioRepository.findByRolNombre(RolNombre.ROLE_ADMIN);
        assertTrue(admins.stream().anyMatch(u -> u.getUsername().equals("adminTest")));
    }
    
    // Actualización

    // 7. Actualizar datos de un usuario y verificar persistencia.
    @Test
    void actualizarDatosUsuarioYVerificarPersistencia() {
        // Buscar el usuario registrado en el setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        assertTrue(usuario != null);

        // Cambiar valores
        usuario.setEmail("correoprueba2@noenviar.com");
        usuario.setNombreResponsable("Carlos");
        usuario.setApellidoResponsable("García");
        usuario.setDocumentoResponsable("87654321");
        usuario.setActivo(false);

        usuarioRepository.save(usuario);

        // Verificar persistencia
        Usuario actualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
        assertEquals("correoprueba2@noenviar.com", actualizado.getEmail());
        assertEquals("Carlos", actualizado.getNombreResponsable());
        assertEquals("García", actualizado.getApellidoResponsable());
        assertEquals("87654321", actualizado.getDocumentoResponsable());
        assertFalse(actualizado.getActivo());
    }
    
    // 8. Actualizar estado de usuario (activo/inactivo).
    @Test
    void actualizarEstadoUsuario() {
        // Usar el usuario registrado en el setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        assertTrue(usuario != null);

        // Cambiar estado a inactivo
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        // Verificar persistencia
        Usuario actualizado = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
        assertFalse(actualizado.getActivo());
    }
    
    // Eliminación

    // 9. Eliminar usuario y verificar que no existe en la base.
    @Test
    void eliminarUsuarioYVerificarNoExisteEnBase() {
        // Usar el usuario registrado en el setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        assertTrue(usuario != null);

        usuarioRepository.delete(usuario);

        boolean existe = usuarioRepository.findById(usuario.getIdUsuario()).isPresent();
        assertFalse(existe);
    }
    
    // Restricciones y reglas

    // 10. No permitir guardar usuarios con username o email duplicado.
    @Test
    void noPermitirGuardarUsuarioConUsernameOEmailDuplicado() {
        // El usuario "usuario123" ya existe por el setUp

        Usuario usuarioDuplicado = new Usuario();
        usuarioDuplicado.setUsername("usuario123"); // mismo username que el del setUp
        usuarioDuplicado.setEmail("correoprueba@noenviar.com"); // mismo email que el del setUp
        usuarioDuplicado.setPassword("OtraClave123");
        usuarioDuplicado.setEmailVerificado(true);
        usuarioDuplicado.setActivo(true);

        Exception exception = null;
        try {
            usuarioRepository.save(usuarioDuplicado);
        } catch (Exception e) {
            exception = e;
        }
        assertTrue(exception != null);
    }
    
    // 11. Verificar que los datos obligatorios se validan correctamente.
    @Test
    void verificarDatosObligatoriosUsuario() {
        Usuario usuario = new Usuario();
        // No se setean datos obligatorios como username y email

        Exception exception = null;
        try {
            usuarioRepository.save(usuario);
        } catch (Exception e) {
            exception = e;
        }
        assertTrue(exception != null);
    }

    // Integridad de relaciones
    
    // 12. Verificar que el usuario está correctamente relacionado con empresa.
    @Test
    void verificarRelacionUsuarioEmpresa() {
        // Usar el usuario registrado en el setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        assertTrue(usuario != null);

        // Crear empresa y asociarla al usuario
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTest");
        empresa.setCuit("12345678901");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);

        perfilEmpresaRepository.save(empresa);

        // Verificar la relación
        PerfilEmpresa empresaGuardada = perfilEmpresaRepository.findById(empresa.getIdPerfilEmpresa()).orElse(null);
        assertTrue(empresaGuardada != null);
        assertEquals(usuario.getIdUsuario(), empresaGuardada.getUsuario().getIdUsuario());
    }
    
    // 13. Verificar que al eliminar usuario, se elimina la empresa asociada.
    @Test
    void eliminarUsuarioYVerificarEliminacionCascadaEmpresa() {
        // Usar el usuario registrado en el setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElse(null);
        assertTrue(usuario != null);

        // Crear empresa y asociarla al usuario
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaTest");
        empresa.setCuit("12345678901");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);

        perfilEmpresaRepository.save(empresa);

        // Eliminar usuario
        usuarioRepository.delete(usuario);

        // Verificar que la empresa asociada también fue eliminada
        boolean empresaExiste = perfilEmpresaRepository.findById(empresa.getIdPerfilEmpresa()).isPresent();
        assertFalse(empresaExiste);
    }
    
}
