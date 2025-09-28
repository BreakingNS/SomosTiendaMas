package com.breakingns.SomosTiendaMas.test.Modulo1.controller;

// Imports Java estándar
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Imports externos
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import jakarta.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

// Imports del proyecto
import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;

/*                                                  AuthControllerIntegrationTest
    
    Registro de usuario

        1. Registro exitoso con datos válidos
        2. Registro falla con email inválido
        3. Registro falla con username inválido
        4. Registro falla con password inválida
        5. Registro falla si el usuario ya existe
        6. Registro falla si el email ya existe

    Login

        7. Login exitoso con credenciales válidas
        8. Login falla con credenciales inválidas
        9. Login falla con usuario no existente

    Generación y validación de tokens

        10. Se generan access y refresh tokens al login
        11. Access token permite acceder a ruta protegida
        12. Refresh token permite obtener nuevos tokens

    Refresh de tokens

        13. Refresh exitoso con refresh token válido
        14. Refresh falla con refresh token inválido
        15. Refresh falla con refresh token expirado
        16. Refresh falla con refresh token revocado

    Logout individual y total

        17. Logout exitoso (revoca sesión y tokens)
        18. Logout total cierra todas las sesiones del usuario
        19. Logout falla si el token es inválido

    Acceso a rutas protegidas

        20. Acceso permitido con token válido
        21. Acceso denegado sin token
        22. Acceso denegado con token inválido
        23. Acceso denegado con token expirado

    Acceso según rol

        24. Acceso permitido a rutas de admin solo con rol admin
        25. Acceso denegado a rutas de admin con rol usuario

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
            // HIJAS -> PADRE (orden evita FK violations)
            "DELETE FROM evento_auditoria",
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
            // Eliminado: DELETE FROM usuario_roles (no existe la tabla)
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )/*,
    @Sql(
        statements = {
            // HIJAS -> PADRE (orden evita FK violations)
            "DELETE FROM evento_auditoria",
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
            // Eliminado: DELETE FROM usuario_roles (no existe la tabla)
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )*/
})
class AuthControllerTest {

    /*          Metodos:
        
        status().isOk() → 200
        status().isUnauthorized() → 401
        status().isForbidden() → 403
        status().isInternalServerError() → 500
        status().isBadRequest() → 400
        status().isCreated() → 201
        status().isNotFound() → 404
        status().isNoContent() → 204
    
    */

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    private final UsuarioServiceImpl usuarioService;
    private final RefreshTokenService refreshTokenService;

    private RegistroUsuarioCompletoDTO registroDTO;

    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;
    private final IDepartamentoRepository departamentoRepository;
    private final ILocalidadRepository localidadRepository;
    private final IMunicipioRepository municipioRepository;
    
    private RegistroDireccionDTO direccionDTO;

    @BeforeEach
    void setUp() throws Exception {
        crearAdminTestSiNoExiste();
    }
    
    void crearAdminTestSiNoExiste() {
        boolean exists = usuarioRepository.findAll().stream()
            .anyMatch(u -> u.getRol() != null && u.getRol().getNombre() == RolNombre.ROLE_ADMIN);
        if (!exists) {
            Usuario admin = new Usuario();
            admin.setUsername("adminTest");
            admin.setPassword("P123456");
            admin.setEmail("correoprueba2@noenviar.com");
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
            admin.setFechaNacimientoResponsable(java.time.LocalDate.of(1990, 1, 1)); // <-- agregado
            usuarioService.registrarConRol(admin, RolNombre.ROLE_SUPERADMIN);
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

    @Test
	void testBasico() {
		assertTrue(true);
	}

    // Registro de usuario
    
    // 1. Registro exitoso con datos válidos
    @Test
    void registroUsuario_exitoso() throws Exception {
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
        usuarioDTO.setRol("ROLE_USUARIO"); // Si quieres especificar el rol

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
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
        
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
     
    // 2. Registro falla con email inválido
    @Test
    void registroUsuario_emailInvalido_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioEmailInvalido");
        usuarioDTO.setEmail("email_invalidohotmail.com"); // Email sin @
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("99999999");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }
    
    // 3. Registro falla con username inválido
    @Test
    void registroUsuario_usernameInvalido_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername(""); // Username inválido
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("33333333");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 4. Registro falla con password inválida
    @Test
    void registroUsuario_passwordInvalida_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioPassInvalida");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("123"); // Password inválida (muy corta)
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("44444444");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }
    
    // 5. Registro falla si el usuario ya existe
    @Test
    void registroUsuario_usuarioExistente_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioExistente");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("55555555");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Primer registro exitoso
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Segundo registro con mismo username
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    // 6. Registro falla si el email ya existe
    @Test
    void registroUsuario_emailExistente_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO1 = new RegistroUsuarioDTO();
        usuarioDTO1.setUsername("usuarioEmail1");
        usuarioDTO1.setEmail("correoprueba@noenviar.com");
        usuarioDTO1.setPassword("P123456");
        usuarioDTO1.setNombreResponsable("Juan");
        usuarioDTO1.setApellidoResponsable("Pérez");
        usuarioDTO1.setDocumentoResponsable("66666666");
        usuarioDTO1.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO1.setAceptaTerminos(true);
        usuarioDTO1.setAceptaPoliticaPriv(true);
        usuarioDTO1.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO1.setGeneroResponsable("MASCULINO");
        usuarioDTO1.setIdioma("es");
        usuarioDTO1.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO1.setRol("ROLE_USUARIO");

        RegistroUsuarioDTO usuarioDTO2 = new RegistroUsuarioDTO();
        usuarioDTO2.setUsername("usuarioEmail2");
        usuarioDTO2.setEmail("correoprueba@noenviar.com"); // mismo email
        usuarioDTO2.setPassword("P123456");
        usuarioDTO2.setNombreResponsable("Juan");
        usuarioDTO2.setApellidoResponsable("Pérez");
        usuarioDTO2.setDocumentoResponsable("77777777");
        usuarioDTO2.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO2.setAceptaTerminos(true);
        usuarioDTO2.setAceptaPoliticaPriv(true);
        usuarioDTO2.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO2.setGeneroResponsable("MASCULINO");
        usuarioDTO2.setIdioma("es");
        usuarioDTO2.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO2.setRol("ROLE_USUARIO");

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO1 = new RegistroUsuarioCompletoDTO();
        registroDTO1.setUsuario(usuarioDTO1);
        registroDTO1.setDirecciones(List.of(direccionDTO));
        registroDTO1.setTelefonos(List.of(telefonoDTO));

        RegistroUsuarioCompletoDTO registroDTO2 = new RegistroUsuarioCompletoDTO();
        registroDTO2.setUsuario(usuarioDTO2);
        registroDTO2.setDirecciones(List.of(direccionDTO));
        registroDTO2.setTelefonos(List.of(telefonoDTO));

        // Primer registro exitoso
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO1)))
                .andReturn();

        // Segundo registro con mismo email
        MvcResult result = mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO2)))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }
    
    // Login

    // 7. Login exitoso con credenciales válidas
    @Test
    void login_exitoso() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("loginUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("88888888");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("loginUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("loginUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(200, loginResult.getResponse().getStatus());
    }

    // 8. Login falla con credenciales inválidas
    @Test
    void login_credencialesInvalidas_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("loginUserFail");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("99999999");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Login con contraseña incorrecta
        LoginRequest loginRequest = new LoginRequest("loginUserFail", "contraFail");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(401, loginResult.getResponse().getStatus());
    }

    // 9. Login falla con usuario no existente
    @Test
    void login_usuarioNoExistente_falla() throws Exception {
        LoginRequest loginRequest = new LoginRequest("usuarioNoExiste", "cualquierPass");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        assertEquals(401, loginResult.getResponse().getStatus());
    }

    // Generación y validación de tokens
    
    // 10. Se generan access y refresh tokens al login
    @Test
    void login_generacionTokens_ok() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("tokenUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("10101010");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("tokenUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("tokenUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String accessToken = null;
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
    }
    
    // 11. Access token permite acceder a ruta protegida
    @Test
    void accessToken_accesoRutaProtegida_ok() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("userProtegido");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("12121212");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("userProtegido");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("userProtegido", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Acceso a ruta protegida (ejemplo: logout privado)
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // 12. Refresh token permite obtener nuevos tokens
    @Test
    void refreshToken_obtieneNuevosTokens_ok() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("userRefresh");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("13131313");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("userRefresh");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("userRefresh", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Refresh token (endpoint real, ajusta si tu endpoint es diferente)
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, refreshResult.getResponse().getStatus());
    }
    
    // Refresh de tokens

    // 13. Refresh exitoso con refresh token válido
    @Test
    void refreshToken_exitoso() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("refreshOkUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("14141414");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("refreshOkUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("refreshOkUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String refreshToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(200, refreshResult.getResponse().getStatus());
    }

    // 14. Refresh falla con refresh token inválido
    @Test
    void refreshToken_invalido_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("refreshFailUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("15151515");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        String refreshTokenInvalido = "tokenInvalido123";
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/public/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshTokenInvalido + "\"}"))
                .andReturn();

        assertEquals(401, refreshResult.getResponse().getStatus());
    }
    
    // 15. Refresh falla con refresh token expirado
    @Test
    void refreshToken_expirado_falla() throws Exception {
        // Registro completo del usuario
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("refreshExpUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("16161616");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("refreshExpUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login y obtener refresh token
        LoginRequest loginRequest = new LoginRequest("refreshExpUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Intentar refresh con el token expirado
        MvcResult result = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        cookies = loginResult.getResponse().getCookies();
        refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Esperar para simular expiración (ajusta el tiempo según tu configuración)
        Thread.sleep(8000);

        // Intentar refresh con el token expirado
        result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // 16. Refresh falla con refresh token revocado
    @Test
    void refreshToken_revocado_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("refreshRevUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("17171717");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("refreshRevUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("refreshRevUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie[] cookies = loginResult.getResponse().getCookies();
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(refreshToken);

        // Revocar el refresh token (debes tener el servicio disponible en el test)
        refreshTokenService.revocarToken(refreshToken);

        MvcResult result = mockMvc.perform(post("/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // Logout individual y total
    
    // 17. Logout exitoso (revoca sesión y tokens)
    @Test
    void logout_exitoso() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("logoutUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("18181818");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("logoutUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("logoutUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, logoutResult.getResponse().getStatus());

        // Verificar que el access token está revocado en la BD
        var tokenEmitidoOpt = tokenEmitidoRepository.findByToken(accessToken);
        assertTrue(tokenEmitidoOpt.isPresent());
        assertTrue(tokenEmitidoOpt.get().isRevocado());

        // Verificar que el refresh token está revocado en la BD
        var sesionOpt = sesionActivaRepository.findByToken(accessToken);
        assertTrue(sesionOpt.isPresent());
        var refreshToken = sesionOpt.get().getRefreshToken();
        var refreshTokenOpt = refreshTokenRepository.findByToken(refreshToken);
        assertTrue(refreshTokenOpt.isPresent());
        assertTrue(refreshTokenOpt.get().getRevocado());
    }

    // 18. Logout total cierra todas las sesiones del usuario
    @Test
    void logoutTotal_revocaTodasSesionesYTokens() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("multiSesionUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("19191919");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("multiSesionUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login 1: user-agent y IP distintos
        LoginRequest loginRequest = new LoginRequest("multiSesionUser", "P123456");
        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent1")
                .with(request -> { request.setRemoteAddr("1.1.1.1"); return request; }))
                .andReturn();

        // Login 2
        mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent2")
                .with(request -> { request.setRemoteAddr("2.2.2.2"); return request; }))
                .andReturn();

        // Login 3
        MvcResult loginResult3 = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .header("User-Agent", "Agent3")
                .with(request -> { request.setRemoteAddr("3.3.3.3"); return request; }))
                .andReturn();

        // Obtener access token de la última sesión (para hacer logout total)
        String accessToken = null;
        for (Cookie cookie : loginResult3.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Logout total
        MvcResult logoutTotalResult = mockMvc.perform(post("/api/auth/private/logout-total")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, logoutTotalResult.getResponse().getStatus());

        // Validar que todas las sesiones del usuario están revocadas
        var sesiones = sesionActivaRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(sesiones.stream().allMatch(SesionActiva::isRevocado), "Todas las sesiones deben estar revocadas");

        // Validar que todos los access tokens están revocados
        var accessTokens = tokenEmitidoRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(accessTokens.stream().allMatch(t -> t.isRevocado()), "Todos los access tokens deben estar revocados");

        // Validar que todos los refresh tokens están revocados
        var refreshTokens = refreshTokenRepository.findAllByUsuario_Username("multiSesionUser");
        assertTrue(refreshTokens.stream().allMatch(t -> t.getRevocado()), "Todos los refresh tokens deben estar revocados");
    }
    
    // 19. Logout falla si el token es inválido
    @Test
    void logout_tokenInvalido_falla() throws Exception {
        String accessTokenInvalido = "tokenInvalido123";
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessTokenInvalido)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, logoutResult.getResponse().getStatus());
    }

    // Acceso a rutas protegidas

    // 20. Acceso permitido con token válido
    @Test
    void accesoRutaProtegida_tokenValido_ok() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("accesoUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("20202020");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("accesoUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("accesoUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Acceso a ruta protegida
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    // 21. Acceso denegado sin token
    @Test
    void accesoRutaProtegida_sinToken_falla() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }
    
    // 22. Acceso denegado con token inválido
    @Test
    void accesoRutaProtegida_tokenInvalido_falla() throws Exception {
        String accessTokenInvalido = "tokenInvalido123";
        MvcResult result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessTokenInvalido)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // 23. Acceso denegado con token expirado
    @Test
    void accesoRutaProtegida_tokenExpirado_falla() throws Exception {
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("expiraUser");
        usuarioDTO.setEmail("correoprueba@noenviar.com");
        usuarioDTO.setPassword("P123456");
        usuarioDTO.setNombreResponsable("Juan");
        usuarioDTO.setApellidoResponsable("Pérez");
        usuarioDTO.setDocumentoResponsable("23232323");
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
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        direccionDTO = new RegistroDireccionDTO();
        direccionDTO.setIdPais(pais.getId());
        direccionDTO.setIdProvincia(provincia.getId());
        direccionDTO.setIdDepartamento(departamento.getId());
        direccionDTO.setIdMunicipio(municipio.getId());
        direccionDTO.setIdLocalidad(localidad.getId());
        direccionDTO.setIdPerfilEmpresa(null);
        direccionDTO.setTipo("PERSONAL");
        direccionDTO.setCalle("Calle Falsa");
        direccionDTO.setNumero("123");
        direccionDTO.setActiva(true);
        direccionDTO.setEsPrincipal(true);
        direccionDTO.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO = new RegistroTelefonoDTO();
        telefonoDTO.setTipo("PRINCIPAL");
        telefonoDTO.setNumero("1122334455");
        telefonoDTO.setCaracteristica("11");
        telefonoDTO.setActivo(true);
        telefonoDTO.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of(direccionDTO));
        registroDTO.setTelefonos(List.of(telefonoDTO));

        // Registrar usuario
        mockMvc.perform(post("/api/gestionusuario/public/usuario/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andReturn();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("expiraUser");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Login
        LoginRequest loginRequest = new LoginRequest("expiraUser", "P123456");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String accessToken = null;
        String refreshToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Intentar refresh con el token expirado
        MvcResult result = mockMvc.perform(post("/test/api/auth/public/refresh-token")
                .header("User-Agent", "MockMvc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andReturn();

        accessToken = null;
        for (Cookie cookie : loginResult.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            }
        }
        assertNotNull(accessToken);

        // Simular expiración del token (ajusta el tiempo según tu configuración)
        Thread.sleep(2000);

        result = mockMvc.perform(post("/api/auth/private/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(401, result.getResponse().getStatus());
    }

    // Acceso según rol

    // 24. Acceso permitido a rutas de admin solo con rol admin
    @Test
    void soloAdminPuedeVerSesionesDeUsuarios_controlado() throws Exception {

        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("adminTest");
        assertTrue(adminOpt.isPresent());

        // Registrar usuario 1
        RegistroUsuarioDTO usuarioDTO1 = new RegistroUsuarioDTO();
        usuarioDTO1.setUsername("usuario1");
        usuarioDTO1.setEmail("correoprueba@noenviar.com");
        usuarioDTO1.setPassword("P123456");
        usuarioDTO1.setNombreResponsable("Juan");
        usuarioDTO1.setApellidoResponsable("Pérez");
        usuarioDTO1.setDocumentoResponsable("24242424");
        usuarioDTO1.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO1.setAceptaTerminos(true);
        usuarioDTO1.setAceptaPoliticaPriv(true);
        usuarioDTO1.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO1.setGeneroResponsable("MASCULINO");
        usuarioDTO1.setIdioma("es");
        usuarioDTO1.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO1.setRol("ROLE_USUARIO");

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        RegistroDireccionDTO direccionDTO1 = new RegistroDireccionDTO();
        direccionDTO1.setIdPais(pais.getId());
        direccionDTO1.setIdProvincia(provincia.getId());
        direccionDTO1.setIdDepartamento(departamento.getId());
        direccionDTO1.setIdMunicipio(municipio.getId());
        direccionDTO1.setIdLocalidad(localidad.getId());
        direccionDTO1.setIdPerfilEmpresa(null);
        direccionDTO1.setTipo("PERSONAL");
        direccionDTO1.setCalle("Calle Falsa");
        direccionDTO1.setNumero("123");
        direccionDTO1.setActiva(true);
        direccionDTO1.setEsPrincipal(true);
        direccionDTO1.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO1 = new RegistroTelefonoDTO();
        telefonoDTO1.setTipo("PRINCIPAL");
        telefonoDTO1.setNumero("1122334455");
        telefonoDTO1.setCaracteristica("11");
        telefonoDTO1.setActivo(true);
        telefonoDTO1.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO1 = new RegistroUsuarioCompletoDTO();
        registroDTO1.setUsuario(usuarioDTO1);
        registroDTO1.setDirecciones(List.of(direccionDTO1));
        registroDTO1.setTelefonos(List.of(telefonoDTO1));

        int statusUser1 = registrarUsuarioCompleto(registroDTO1);
        assertEquals(200, statusUser1);
        Optional<Usuario> user1Opt = usuarioRepository.findByUsername("usuario1");
        assertTrue(user1Opt.isPresent());
        Usuario usuario1 = user1Opt.get();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario1");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Registrar usuario 2
        RegistroUsuarioDTO usuarioDTO2 = new RegistroUsuarioDTO();
        usuarioDTO2.setUsername("usuario2");
        usuarioDTO2.setEmail("correoprueba1@noenviar.com");
        usuarioDTO2.setPassword("P123456");
        usuarioDTO2.setNombreResponsable("Juan");
        usuarioDTO2.setApellidoResponsable("Pérez");
        usuarioDTO2.setDocumentoResponsable("25252525");
        usuarioDTO2.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO2.setAceptaTerminos(true);
        usuarioDTO2.setAceptaPoliticaPriv(true);
        usuarioDTO2.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO2.setGeneroResponsable("MASCULINO");
        usuarioDTO2.setIdioma("es");
        usuarioDTO2.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO2.setRol("ROLE_USUARIO");

        RegistroDireccionDTO direccionDTO2 = new RegistroDireccionDTO();
        direccionDTO2.setIdPais(pais.getId());
        direccionDTO2.setIdProvincia(provincia.getId());
        direccionDTO2.setIdDepartamento(departamento.getId());
        direccionDTO2.setIdMunicipio(municipio.getId());
        direccionDTO2.setIdLocalidad(localidad.getId());
        direccionDTO2.setIdPerfilEmpresa(null);
        direccionDTO2.setTipo("PERSONAL");
        direccionDTO2.setCalle("Calle Falsa");
        direccionDTO2.setNumero("123");
        direccionDTO2.setActiva(true);
        direccionDTO2.setEsPrincipal(true);
        direccionDTO2.setCodigoPostal("1000");


        RegistroTelefonoDTO telefonoDTO2 = new RegistroTelefonoDTO();
        telefonoDTO2.setTipo("PRINCIPAL");
        telefonoDTO2.setNumero("1122334455");
        telefonoDTO2.setCaracteristica("11");
        telefonoDTO2.setActivo(true);
        telefonoDTO2.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO2 = new RegistroUsuarioCompletoDTO();
        registroDTO2.setUsuario(usuarioDTO2);
        registroDTO2.setDirecciones(List.of(direccionDTO2));
        registroDTO2.setTelefonos(List.of(telefonoDTO2));

        int statusUser2 = registrarUsuarioCompleto(registroDTO2);
        assertEquals(200, statusUser2);
        Optional<Usuario> user2Opt = usuarioRepository.findByUsername("usuario2");
        assertTrue(user2Opt.isPresent());
        Usuario usuario2 = user2Opt.get();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt2 = usuarioRepository.findByUsername("usuario2");
        assertTrue(usuarioOpt2.isPresent());
        Usuario usuario3 = usuarioOpt2.get();
        usuario3.setEmailVerificado(true);
        usuarioRepository.save(usuario3);

        // Login admin
        MvcResult loginResultAdmin = loginUsuario("adminTest", "P123456");
        assertEquals(200, loginResultAdmin.getResponse().getStatus());
        String adminAccessToken = null;
        for (Cookie cookie : loginResultAdmin.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                adminAccessToken = cookie.getValue();
            }
        }
        assertNotNull(adminAccessToken);

        // Login Usuario 1
        MvcResult loginResultUsuario1 = loginUsuario("usuario1", "P123456");
        assertEquals(200, loginResultUsuario1.getResponse().getStatus());
        String usuario1AccessToken = null;
        for (Cookie cookie : loginResultUsuario1.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                usuario1AccessToken = cookie.getValue();
            }
        }
        assertNotNull(usuario1AccessToken);

        // Login Usuario 2
        MvcResult loginResultUsuario2 = loginUsuario("usuario2", "P123456");
        assertEquals(200, loginResultUsuario2.getResponse().getStatus());
        String usuario2AccessToken = null;
        for (Cookie cookie : loginResultUsuario2.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                usuario2AccessToken = cookie.getValue();
            }
        }
        assertNotNull(usuario2AccessToken);


        // Admin accede y puede ver sesiones activas de usuario 1
        MvcResult resultAdminUser1 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + adminAccessToken)
                .param("idUsuario", String.valueOf(usuario1.getIdUsuario()))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, resultAdminUser1.getResponse().getStatus());

        // Admin accede y puede ver sesiones activas de usuario 2
        MvcResult resultAdminUser2 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + adminAccessToken)
                .param("idUsuario", String.valueOf(usuario2.getIdUsuario()))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(200, resultAdminUser2.getResponse().getStatus());
    }
    
    // 25. Acceso denegado a rutas de admin con rol usuario
    @Test
    void accesoAdmin_conRolUsuario_falla() throws Exception {
        // Registrar usuario 1
        RegistroUsuarioDTO usuarioDTO1 = new RegistroUsuarioDTO();
        usuarioDTO1.setUsername("usuario1");
        usuarioDTO1.setEmail("correoprueba@noenviar.com");
        usuarioDTO1.setPassword("P123456");
        usuarioDTO1.setNombreResponsable("Juan");
        usuarioDTO1.setApellidoResponsable("Pérez");
        usuarioDTO1.setDocumentoResponsable("24242424");
        usuarioDTO1.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO1.setAceptaTerminos(true);
        usuarioDTO1.setAceptaPoliticaPriv(true);
        usuarioDTO1.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO1.setGeneroResponsable("MASCULINO");
        usuarioDTO1.setIdioma("es");
        usuarioDTO1.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO1.setRol("ROLE_USUARIO");

        Pais pais = paisRepository.findByNombre("Argentina");
        Provincia provincia = provinciaRepository.findByNombreAndPais("CATAMARCA", pais);
        Departamento departamento = departamentoRepository.findByNombreAndProvincia("CAPITAL", provincia);
        Municipio municipio = municipioRepository.findByNombre("SAN FERNANDO DEL VALLE DE CATAMARCA");
        Localidad localidad = localidadRepository.findByNombreAndMunicipioAndDepartamentoAndProvincia(
            "SAN FERNANDO DEL VALLE DE CATAMARCA", municipio, departamento, provincia
        ).orElseThrow();
        
        RegistroDireccionDTO direccionDTO1 = new RegistroDireccionDTO();
        direccionDTO1.setIdPais(pais.getId());
        direccionDTO1.setIdProvincia(provincia.getId());
        direccionDTO1.setIdDepartamento(departamento.getId());
        direccionDTO1.setIdMunicipio(municipio.getId());
        direccionDTO1.setIdLocalidad(localidad.getId());
        direccionDTO1.setIdPerfilEmpresa(null);
        direccionDTO1.setTipo("PERSONAL");
        direccionDTO1.setCalle("Calle Falsa");
        direccionDTO1.setNumero("123");
        direccionDTO1.setActiva(true);
        direccionDTO1.setEsPrincipal(true);
        direccionDTO1.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO1 = new RegistroTelefonoDTO();
        telefonoDTO1.setTipo("PRINCIPAL");
        telefonoDTO1.setNumero("1122334455");
        telefonoDTO1.setCaracteristica("11");
        telefonoDTO1.setActivo(true);
        telefonoDTO1.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO1 = new RegistroUsuarioCompletoDTO();
        registroDTO1.setUsuario(usuarioDTO1);
        registroDTO1.setDirecciones(List.of(direccionDTO1));
        registroDTO1.setTelefonos(List.of(telefonoDTO1));

        int statusUser1 = registrarUsuarioCompleto(registroDTO1);
        assertEquals(200, statusUser1);
        Optional<Usuario> user1Opt = usuarioRepository.findByUsername("usuario1");
        assertTrue(user1Opt.isPresent());

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuario1");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Registrar usuario 2
        RegistroUsuarioDTO usuarioDTO2 = new RegistroUsuarioDTO();
        usuarioDTO2.setUsername("usuario2");
        usuarioDTO2.setEmail("correoprueba1@noenviar.com");
        usuarioDTO2.setPassword("P123456");
        usuarioDTO2.setNombreResponsable("Juan");
        usuarioDTO2.setApellidoResponsable("Pérez");
        usuarioDTO2.setDocumentoResponsable("25252525");
        usuarioDTO2.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO2.setAceptaTerminos(true);
        usuarioDTO2.setAceptaPoliticaPriv(true);
        usuarioDTO2.setFechaNacimientoResponsable(LocalDate.of(1990, 1, 1));
        usuarioDTO2.setGeneroResponsable("MASCULINO");
        usuarioDTO2.setIdioma("es");
        usuarioDTO2.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO2.setRol("ROLE_USUARIO");

        RegistroDireccionDTO direccionDTO2 = new RegistroDireccionDTO();
        direccionDTO2.setIdPais(pais.getId());
        direccionDTO2.setIdProvincia(provincia.getId());
        direccionDTO2.setIdDepartamento(departamento.getId());
        direccionDTO2.setIdMunicipio(municipio.getId());
        direccionDTO2.setIdLocalidad(localidad.getId());
        direccionDTO2.setIdPerfilEmpresa(null);
        direccionDTO2.setTipo("PERSONAL");
        direccionDTO2.setCalle("Calle Falsa");
        direccionDTO2.setNumero("123");
        direccionDTO2.setActiva(true);
        direccionDTO2.setEsPrincipal(true);
        direccionDTO2.setCodigoPostal("1000");

        RegistroTelefonoDTO telefonoDTO2 = new RegistroTelefonoDTO();
        telefonoDTO2.setTipo("PRINCIPAL");
        telefonoDTO2.setNumero("1122334455");
        telefonoDTO2.setCaracteristica("11");
        telefonoDTO2.setActivo(true);
        telefonoDTO2.setVerificado(false);

        RegistroUsuarioCompletoDTO registroDTO2 = new RegistroUsuarioCompletoDTO();
        registroDTO2.setUsuario(usuarioDTO2);
        registroDTO2.setDirecciones(List.of(direccionDTO2));
        registroDTO2.setTelefonos(List.of(telefonoDTO2));

        int statusUser2 = registrarUsuarioCompleto(registroDTO2);
        assertEquals(200, statusUser2);
        Optional<Usuario> user2Opt = usuarioRepository.findByUsername("usuario2");
        assertTrue(user2Opt.isPresent());
        Usuario usuario2 = user2Opt.get();

        // Asegura que el usuario y el email están verificados
        Optional<Usuario> usuarioOpt2 = usuarioRepository.findByUsername("usuario2");
        assertTrue(usuarioOpt2.isPresent());
        Usuario usuario3 = usuarioOpt2.get();
        usuario3.setEmailVerificado(true);
        usuarioRepository.save(usuario3);

        // Login usuario 1
        MvcResult loginResultUsuario1 = loginUsuario("usuario1", "P123456");
        assertEquals(200, loginResultUsuario1.getResponse().getStatus());
        String usuario1AccessToken = null;
        for (Cookie cookie : loginResultUsuario1.getResponse().getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                usuario1AccessToken = cookie.getValue();
            }
        }
        assertNotNull(usuario1AccessToken);

        // Login usuario 2 (por si el endpoint requiere que ambos tengan sesión)
        MvcResult loginResultUsuario2 = loginUsuario("usuario2", "P123456");
        assertEquals(200, loginResultUsuario2.getResponse().getStatus());

        // Usuario 1 intenta acceder a endpoint solo admin para ver sesiones de usuario 2
        MvcResult resultUser1 = mockMvc.perform(get("/api/sesiones/private/admin/activas")
                .header("Authorization", "Bearer " + usuario1AccessToken)
                .param("idUsuario", String.valueOf(usuario2.getIdUsuario()))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals(403, resultUser1.getResponse().getStatus());
    }
}