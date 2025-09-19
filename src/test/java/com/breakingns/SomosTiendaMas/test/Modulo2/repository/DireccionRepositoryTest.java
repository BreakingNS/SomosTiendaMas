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
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*                                                  DireccionRepositoryTest

    Persistencia básica

        1. Guardar una dirección correctamente en la base de datos.
        2. Buscar dirección por ID.
        3. Buscar direcciones por usuario.
        4. Buscar direcciones por perfil empresa.
        5. Actualizar datos de una dirección y verificar persistencia.
        6. Eliminar dirección y verificar que no existe en la base.

    Eliminación en cascada

        7. Eliminar usuario y verificar que se eliminan sus direcciones asociadas.
        8. Eliminar perfil empresa y verificar que se eliminan sus direcciones asociadas.

    Restricciones y reglas

        9. No permitir guardar dirección sin datos obligatorios (calle, número, código postal, país, tipo).
        10. No permitir guardar dirección duplicada para el mismo usuario (si aplica).
        11. No permitir guardar dirección duplicada para la misma empresa (si aplica).

    Relaciones

        12. Verificar que se puede asociar una dirección copiada a otro usuario o empresa.
        13. Verificar que la dirección está correctamente relacionada con usuario y/o empresa.

    Consultas avanzadas

        14. Buscar direcciones activas/inactivas por usuario.
        15. Buscar direcciones principales por usuario.
        16. Buscar direcciones por país/provincia/localidad.

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
public class DireccionRepositoryTest {
    
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

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
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

        // 8. Registrar dirección
        mockMvc.perform(post("/api/direccion/public/registrar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(direccionDTO)));
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

    // Persistencia básica
    /*
    @Test
    void testFuncionamientoRegistro() throws Exception {
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

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
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

        // 8. Registrar dirección
        MvcResult result = mockMvc.perform(post("/api/direccion/public/registrar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(direccionDTO)))
            .andReturn();
    }*/
    
    // 1. Guardar una dirección correctamente en la base de datos.
    @Test
    void guardarDireccionEnvioCorrectamente() throws Exception {

        // 2. Buscar usuario registrado
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // 3. Instanciar dirección tipo ENVIO, no principal
        RegistroDireccionDTO direccionDTO = crearDireccionParaUsuario(
            usuario,
            "ENVIO",
            "Calle de Envío",
            "456",
            false // esPrincipal
        );

        // 4. Registrar dirección
        MvcResult result = registrarDireccion(direccionDTO);
        assertEquals(200, result.getResponse().getStatus());

        // 5. Verificar que la dirección ENVIO fue guardada y no es principal
        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        System.out.println("Dirs repo: " + dirs);

        // imprimir campos para depurar exactamente qué valores llegaron
        dirs.forEach(d -> System.out.println("dir id=" + d.getIdDireccion() 
            + " tipo='" + d.getTipo() + "'" 
            + " calle='" + d.getCalle() + "'" 
            + " esPrincipal=" + d.getEsPrincipal()
            + " idUsuario=" + (d.getUsuario() != null ? d.getUsuario().getIdUsuario() : "null")));

        // comparación más tolerante (ignora mayúsculas/espacios)
        boolean existeEnvio = dirs.stream().anyMatch(d -> {
            String tipo = d.getTipo() == null ? "" : d.getTipo().name();
            String calle = d.getCalle() == null ? "" : d.getCalle().trim();
            return "ENVIO".equalsIgnoreCase(tipo.trim()) && "Calle de Envío".equalsIgnoreCase(calle);
        });
        assertTrue(existeEnvio);
    }
    
    // 2. Buscar dirección por ID.
    @Test
    void buscarDireccionPorId() throws Exception {
        // recuperar usuario creado en setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // obtener al menos una dirección asociada al usuario (creada en setUp)
        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertFalse(dirs.isEmpty(), "No se encontraron direcciones para el usuario de prueba");

        // tomar la primera dirección y buscarla por id
        Direccion direccionEsperada = dirs.get(0);
        Long idDireccion = direccionEsperada.getIdDireccion();

        Direccion encontrada = direccionRepository.findById(idDireccion).orElseThrow();

        // verificaciones básicas de consistencia
        assertEquals(idDireccion, encontrada.getIdDireccion());
        assertEquals(usuario.getIdUsuario(), encontrada.getUsuario() != null ? encontrada.getUsuario().getIdUsuario() : null);
        assertEquals(direccionEsperada.getTipo(), encontrada.getTipo());
        assertEquals(direccionEsperada.getCalle().trim(), encontrada.getCalle() != null ? encontrada.getCalle().trim() : null);
        assertEquals(direccionEsperada.getNumero(), encontrada.getNumero());
        // verificar bandera de principal si estaba marcada en el setUp
        assertEquals(direccionEsperada.getEsPrincipal(), encontrada.getEsPrincipal());
    }

    // 3. Buscar direcciones por usuario.
    @Test
    void buscarDireccionesPorUsuario() {
        // recuperar usuario creado en setUp
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // buscar direcciones por id de usuario
        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        // debe traer al menos la dirección creada en el setUp
        assertFalse(dirs.isEmpty(), "No se encontraron direcciones para el usuario de prueba");

        // todas las direcciones deben pertenecer al mismo usuario
        assertTrue(dirs.stream().allMatch(d -> d.getUsuario() != null
                && usuario.getIdUsuario().equals(d.getUsuario().getIdUsuario())),
            "Hay direcciones asociadas a otro usuario");

        // verificar que existe la dirección creada en setUp (PERSONAL, Calle Falsa)
        boolean tieneCalleFalsa = dirs.stream().anyMatch(d -> {
            String tipo = d.getTipo() == null ? "" : d.getTipo().name();
            String calle = d.getCalle() == null ? "" : d.getCalle().trim();
            return "PERSONAL".equalsIgnoreCase(tipo) && "Calle Falsa".equalsIgnoreCase(calle);
        });
        assertTrue(tieneCalleFalsa, "No se encontró la dirección esperada 'Calle Falsa' de tipo PERSONAL");
    }

    // 4. Buscar direcciones por perfil empresa.
    @Test
    void buscarDireccionesPorPerfilEmpresa() throws Exception {

        // usuario ya creado en @BeforeEach
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

        // crear dirección para la empresa (usar helper para obtener ids de ubicación)
        RegistroDireccionDTO direccionEmpresaDTO = crearDireccionParaUsuario(usuario, "FISCAL", "Calle Empresa", "789", true);
        direccionEmpresaDTO.setIdUsuario(null); // nulo para empresa
        direccionEmpresaDTO.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());

        // registrar dirección vinculada a perfil empresa
        MvcResult result = registrarDireccion(direccionEmpresaDTO);
        assertEquals(200, result.getResponse().getStatus());

        // recuperar direcciones por id de perfil empresa y verificar
        List<Direccion> dirsEmpresa = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertFalse(dirsEmpresa.isEmpty(), "No se encontraron direcciones para el perfil empresa");

        boolean existeFiscal = dirsEmpresa.stream().anyMatch(d -> {
            String tipo = d.getTipo() == null ? "" : d.getTipo().name();
            String calle = d.getCalle() == null ? "" : d.getCalle().trim();
            return "FISCAL".equalsIgnoreCase(tipo) &&
                   "Calle Empresa".equalsIgnoreCase(calle) &&
                   d.getPerfilEmpresa() != null &&
                   empresa.getIdPerfilEmpresa().equals(d.getPerfilEmpresa().getIdPerfilEmpresa());
        });
        assertTrue(existeFiscal, "No se encontró la dirección FISCAL asociada al perfil empresa");
    }

    // 5. Actualizar datos de una dirección y verificar persistencia.
    @Test
    void actualizarDatosDireccionYVerificarPersistencia() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y persistir una dirección nueva para actualizar
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Temp", "999", false);
        MvcResult result = registrarDireccion(dto);
        assertEquals(200, result.getResponse().getStatus());

        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertFalse(dirs.isEmpty(), "No se creó la dirección para actualizar");

        Direccion target = dirs.stream()
            .filter(d -> "999".equals(d.getNumero()) && "Calle Temp".equalsIgnoreCase(d.getCalle()))
            .findFirst()
            .orElse(dirs.get(0));

        Long id = target.getIdDireccion();
        Direccion entidad = direccionRepository.findById(id).orElseThrow();

        // modificar y persistir
        entidad.setCalle("Calle Actualizada");
        entidad.setNumero("1000");
        entidad.setEsPrincipal(true);
        direccionRepository.save(entidad);

        // verificar cambios
        Direccion actualizado = direccionRepository.findById(id).orElseThrow();
        assertEquals("Calle Actualizada", actualizado.getCalle().trim());
        assertEquals("1000", actualizado.getNumero());
        assertTrue(actualizado.getEsPrincipal());
    }

    // 6. Eliminar dirección y verificar que no existe en la base.
    @Test
    void eliminarDireccionYVerificarNoExisteEnBase() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y persistir una dirección que luego se eliminará
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Eliminar", "321", false);
        MvcResult result = registrarDireccion(dto);
        assertEquals(200, result.getResponse().getStatus());

        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        Direccion target = dirs.stream()
            .filter(d -> "321".equals(d.getNumero()) && "Calle Eliminar".equalsIgnoreCase(d.getCalle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró la dirección creada para eliminar"));

        Long id = target.getIdDireccion();

        // eliminar y verificar
        direccionRepository.deleteById(id);
        assertFalse(direccionRepository.findById(id).isPresent(), "La dirección no fue eliminada de la base");

        List<Direccion> after = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(after.stream().noneMatch(d -> id.equals(d.getIdDireccion())));
    }

    // Eliminación en cascada

    // 7. Eliminar usuario y verificar que se eliminan sus direcciones asociadas.
    @Test
    void eliminarUsuarioYVerificarEliminacionCascadaDirecciones() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y persistir una dirección para el usuario
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Para Borrar", "777", false);
        MvcResult result = registrarDireccion(dto);
        assertEquals(200, result.getResponse().getStatus());

        List<Direccion> antes = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertFalse(antes.isEmpty(), "No se creó la dirección para el usuario antes de eliminarlo");

        // eliminar usuario
        usuarioRepository.deleteById(usuario.getIdUsuario());

        // refrescar/consultar direcciones del usuario eliminado
        List<Direccion> despues = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(despues.isEmpty(), "Las direcciones del usuario deberían eliminarse en cascada al borrar el usuario");
    }

    // 8. Eliminar perfil empresa y verificar que se eliminan sus direcciones asociadas.
    @Test
    void eliminarPerfilEmpresaYVerificarEliminacionCascadaDirecciones() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear y guardar perfil empresa asociado al usuario
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaParaBorrar");
        empresa.setCuit("20999999999");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresa@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa);

        // crear dirección vinculada a la empresa
        RegistroDireccionDTO direccionEmpresaDTO = crearDireccionParaUsuario(usuario, "FISCAL", "Calle Empresa Borrar", "888", true);
        direccionEmpresaDTO.setIdUsuario(null);
        direccionEmpresaDTO.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        MvcResult result = registrarDireccion(direccionEmpresaDTO);
        assertEquals(200, result.getResponse().getStatus());

        List<Direccion> antes = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertFalse(antes.isEmpty(), "No se creó la dirección para el perfil empresa");

        // eliminar perfil empresa
        perfilEmpresaRepository.deleteById(empresa.getIdPerfilEmpresa());

        List<Direccion> despues = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        assertTrue(despues.isEmpty(), "Las direcciones del perfil empresa deberían eliminarse en cascada al borrar el perfil");
    }

    // Restricciones y reglas

    // 9. No permitir guardar dirección sin datos obligatorios.
    @Test
    void noPermitirGuardarDireccionSinDatosObligatorios() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "ENVIO", null, null, false);
        // quitar campos obligatorios explícitamente
        dto.setCalle(null);
        dto.setNumero(null);
        dto.setCodigoPostal(null);

        MvcResult result = registrarDireccion(dto);
        int status = result.getResponse().getStatus();
        // esperar que el endpoint no acepte (400 o 422). Aceptamos cualquier código de error >=400
        System.out.println("\n\nStatus al intentar guardar sin datos obligatorios: " + status);
        assertEquals(503, status, "Se esperaba 503 por fallo en la persistencia");
    }

    // 10. No permitir guardar dirección duplicada para el mismo usuario.
    @Test
    void noPermitirGuardarDireccionDuplicadaParaUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        RegistroDireccionDTO dto1 = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Duplicada", "101", false);
        MvcResult r1 = registrarDireccion(dto1);
        assertEquals(200, r1.getResponse().getStatus());

        RegistroDireccionDTO dto2 = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Duplicada", "101", false);
        registrarDireccion(dto2);
        // no asumimos comportamiento por status, verificamos el estado de la BD

        // recargar direcciones del usuario y contar las iguales
        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long iguales = dirs.stream()
            .filter(d -> "Calle Duplicada".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())
                      && "101".equals(d.getNumero()))
            .count();

        // esperar que exista solo una dirección igual para ese usuario
        assertEquals(1, iguales, "Debe existir exactamente una dirección con los mismos datos para el usuario (iguales=" + iguales + ")");
    }

    // 11. No permitir guardar dirección duplicada para la misma empresa (solo idPerfilEmpresa, idUsuario = null).
    @Test
    void noPermitirGuardarDireccionDuplicadaParaEmpresa() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaDuplicado");
        empresa.setCuit("20988888888");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresaDup@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa);

        // crear DTO para la empresa: idUsuario NULL, idPerfilEmpresa = empresa.getIdPerfilEmpresa()
        RegistroDireccionDTO dto1 = crearDireccionParaUsuario(usuario, "FISCAL", "Calle Empresa Dup", "202", true);
        dto1.setIdUsuario(null);
        dto1.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());

        MvcResult r1 = registrarDireccionExpectOk(dto1);
        assertEquals(200, r1.getResponse().getStatus());

        // intentar crear la misma dirección para la misma empresa
        RegistroDireccionDTO dto2 = crearDireccionParaUsuario(usuario, "FISCAL", "Calle Empresa Dup", "202", true);
        dto2.setIdUsuario(null);
        dto2.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        MvcResult r2 = registrarDireccion(dto2);

        // recargar direcciones vinculadas a la empresa y contar las iguales
        List<Direccion> dirs = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        long iguales = dirs.stream()
            .filter(d -> "Calle Empresa Dup".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())
                      && "202".equals(d.getNumero()))
            .count();

        // además verificar que las direcciones encontradas pertenecen a la empresa (usuario = null)
        boolean ownersCorrect = dirs.stream()
            .filter(d -> "Calle Empresa Dup".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())
                      && "202".equals(d.getNumero()))
            .allMatch(d -> d.getUsuario() == null && d.getPerfilEmpresa() != null
                        && empresa.getIdPerfilEmpresa().equals(d.getPerfilEmpresa().getIdPerfilEmpresa()));

        // Aceptamos dos comportamientos: el endpoint rechaza el duplicado (error >=400) o se reusa (solo 1 registro igual).
        assertTrue(r2.getResponse().getStatus() >= 400 || iguales == 1,
            "No debe permitirse duplicar la misma dirección para una misma empresa (status=" + r2.getResponse().getStatus() + ", iguales=" + iguales + ")");
        assertTrue(ownersCorrect, "Las direcciones iguales deben estar correctamente asociadas a la empresa y sin usuario asociado");
    }

    // Relaciones

    // 12. Verificar que se puede "copiar" una dirección a otro usuario creando una fila independiente.
    @Test
    void asociarDireccionCopiadaAOtroUsuarioOEmpresa_sinEsCopiaDe_creaFilaIndependiente() throws Exception {
        Usuario usuarioA = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear dirección original para A
        RegistroDireccionDTO originalDto = crearDireccionParaUsuario(usuarioA, "PERSONAL", "Calle Original", "303", false);
        MvcResult rOrig = registrarDireccion(originalDto);
        assertEquals(200, rOrig.getResponse().getStatus());

        List<Direccion> dirsA = direccionRepository.findByUsuario_IdUsuario(usuarioA.getIdUsuario());
        Direccion original = dirsA.stream()
            .filter(d -> "Calle Original".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim()))
            .findFirst().orElseThrow();

        // crear un nuevo usuario B
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioCopiado");
        usuarioDTO.setEmail("copiado@noenviar.com");
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

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("2233445566");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroB = new RegistroUsuarioCompletoDTO();
        registroB.setUsuario(usuarioDTO);
        registroB.setTelefonos(List.of(telefonoDTO));
        registroB.setDirecciones(List.of());

        assertEquals(200, registrarUsuarioCompleto(registroB));
        Usuario usuarioB = usuarioRepository.findByUsername("usuarioCopiado").orElseThrow();

        // sanity: ids distintos
        assertTrue(!usuarioA.getIdUsuario().equals(usuarioB.getIdUsuario()), "Los usuarios A y B deben tener ids distintos");

        // crear una nueva dirección para B con los mismos datos (simula "copiar")
        RegistroDireccionDTO copiaDto = crearDireccionParaUsuario(usuarioB, "PERSONAL", "Calle Original", "303", false);
        MvcResult rCopy = registrarDireccion(copiaDto);
        assertEquals(200, rCopy.getResponse().getStatus());

        // verificar que existe la dirección para B con mismos datos pero distinto id y distinto owner
        List<Direccion> dirsB = direccionRepository.findByUsuario_IdUsuario(usuarioB.getIdUsuario());
        Direccion copia = dirsB.stream()
            .filter(d -> "Calle Original".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())
                       && "303".equals(d.getNumero()))
            .findFirst().orElseThrow();

        // misma data
        assertEquals(original.getCalle().trim().toLowerCase(), copia.getCalle().trim().toLowerCase());
        assertEquals(original.getNumero(), copia.getNumero());

        // distinto id y distinto owner
        assertTrue(!original.getIdDireccion().equals(copia.getIdDireccion()), "La copia debe tener un id distinto a la original");
        assertTrue(!original.getUsuario().getIdUsuario().equals(copia.getUsuario().getIdUsuario()), "La copia debe pertenecer a otro usuario");
    }

    // 13. Verificar que la dirección está correctamente relacionada con usuario y/o empresa.
    @Test
    void verificarRelacionDireccionUsuarioEmpresa() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear dirección para usuario
        RegistroDireccionDTO dtoUser = crearDireccionParaUsuario(usuario, "PERSONAL", "Calle Rel", "404", false);
        MvcResult rUser = registrarDireccion(dtoUser);
        assertEquals(200, rUser.getResponse().getStatus());

        List<Direccion> dirsUser = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        Direccion dUser = dirsUser.stream().filter(d -> "Calle Rel".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())).findFirst().orElseThrow();
        assertEquals(usuario.getIdUsuario(), dUser.getUsuario() != null ? dUser.getUsuario().getIdUsuario() : null);

        // crear perfil empresa y dirección para empresa
        PerfilEmpresa empresa = new PerfilEmpresa();
        empresa.setRazonSocial("EmpresaRel");
        empresa.setCuit("20977777777");
        empresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        empresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        empresa.setEmailEmpresa("empresaRel@noenviar.com");
        empresa.setRequiereFacturacion(true);
        empresa.setFechaCreacion(LocalDateTime.now());
        empresa.setFechaUltimaModificacion(LocalDateTime.now());
        empresa.setUsuario(usuario);
        perfilEmpresaRepository.save(empresa);

        RegistroDireccionDTO dtoEmp = crearDireccionParaUsuario(usuario, "FISCAL", "Calle Rel Empresa", "505", true);
        dtoEmp.setIdUsuario(null);
        dtoEmp.setIdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        MvcResult rEmp = registrarDireccion(dtoEmp);
        assertEquals(200, rEmp.getResponse().getStatus());

        List<Direccion> dirsEmp = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresa.getIdPerfilEmpresa());
        Direccion dEmp = dirsEmp.stream().filter(d -> "Calle Rel Empresa".equalsIgnoreCase(d.getCalle() == null ? "" : d.getCalle().trim())).findFirst().orElseThrow();
        assertEquals(empresa.getIdPerfilEmpresa(), dEmp.getPerfilEmpresa() != null ? dEmp.getPerfilEmpresa().getIdPerfilEmpresa() : null);
    }

    // Consultas avanzadas

    // 14. Buscar direcciones activas/inactivas por usuario.
    @Test
    void buscarDireccionesActivasInactivasPorUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear una activa y una inactiva
        RegistroDireccionDTO act = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Activa", "601", false);
        act.setActiva(true);
        MvcResult rAct = registrarDireccion(act);
        assertEquals(200, rAct.getResponse().getStatus());

        RegistroDireccionDTO inac = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Inactiva", "602", false);
        inac.setActiva(false);
        MvcResult rInac = registrarDireccion(inac);
        assertEquals(200, rInac.getResponse().getStatus());

        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        long activas = dirs.stream().filter(d -> Boolean.TRUE.equals(d.getActiva())).count();
        long inactivas = dirs.stream().filter(d -> Boolean.FALSE.equals(d.getActiva())).count();

        assertTrue(activas >= 1, "Debe existir al menos una dirección activa");
        assertTrue(inactivas >= 1, "Debe existir al menos una dirección inactiva");
    }

    // 15. Buscar direcciones principales por usuario.
    @Test
    void buscarDireccionesPrincipalesPorUsuario() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();

        // crear principal y no principal
        RegistroDireccionDTO pri = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Principal", "701", true);
        MvcResult rPri = registrarDireccion(pri);
        assertEquals(200, rPri.getResponse().getStatus());

        RegistroDireccionDTO noPri = crearDireccionParaUsuario(usuario, "ENVIO", "Calle Secundaria", "702", false);
        MvcResult rNoPri = registrarDireccion(noPri);
        assertEquals(200, rNoPri.getResponse().getStatus());

        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        boolean tienePrincipal = dirs.stream().anyMatch(d -> Boolean.TRUE.equals(d.getEsPrincipal()));
        assertTrue(tienePrincipal, "Debe existir al menos una dirección principal");
    }

    // 16. Buscar direcciones por país/provincia/localidad.
    @Test
    void buscarDireccionesPorPaisProvinciaLocalidad() throws Exception {
        Usuario usuario = usuarioRepository.findByUsername("usuario123").orElseThrow();
        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);

        // crear dirección con esos ids
        RegistroDireccionDTO dto = crearDireccionParaUsuario(usuario, "PERSONAL", "Calle Geo", "801", false);
        MvcResult r = registrarDireccion(dto);
        assertEquals(200, r.getResponse().getStatus());

        List<Direccion> dirs = direccionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        boolean encontrado = dirs.stream().anyMatch(d ->
            d.getProvincia() != null && d.getDepartamento() != null &&
            provincia.getId().equals(d.getProvincia().getId()) &&
            departamento.getId().equals(d.getDepartamento().getId())
        );
        assertTrue(encontrado, "Debe encontrarse dirección con la provincia/departamento especificados");
    }
    
}
