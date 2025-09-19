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
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        /* 
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
        */

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
        /*
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

    // 1) registrarPerfilEmpresa_Valido_OK: registra perfil empresa válido, devuelve DTO con id y campos mapeados.
    @Test
    void registrarPerfilEmpresa_Valido_OK() throws Exception {
    }

    // 2) registrarPerfilEmpresa_CUITExistente_Error: rechaza registro si el CUIT ya existe.
    @Test
    void registrarPerfilEmpresa_CUITExistente_Error() throws Exception {
    }

    // 3) registrarPerfilEmpresa_UsuarioInexistente_Error: rechaza si idUsuario no existe.
    @Test
    void registrarPerfilEmpresa_UsuarioInexistente_Error() throws Exception {
    }

    // 4) registrarPerfilEmpresa_CondicionIVAInvalida_Error: rechaza si condicionIVA no corresponde a enum.
    @Test
    void registrarPerfilEmpresa_CondicionIVAInvalida_Error() throws Exception {
    }

    // 5) actualizarPerfilEmpresa_Valido_OK: actualiza campos permitidos y persiste cambios.
    @Test
    void actualizarPerfilEmpresa_Valido_OK() throws Exception {
    }

    // 6) actualizarPerfilEmpresa_NoEncontrado_Error: actualizar id inexistente lanza IllegalArgumentException.
    @Test
    void actualizarPerfilEmpresa_NoEncontrado_Error() throws Exception {
    }

    // 7) actualizarPerfilEmpresa_CambiosOptionales_OK: actualiza campos opcionales (categoria, sitio, descripcion).
    @Test
    void actualizarPerfilEmpresa_CambiosOptionales_OK() throws Exception {
    }

    // 8) obtenerPerfilEmpresa_OK: devuelve PerfilEmpresaResponseDTO con idUsuario y demás campos correctamente mapeados.
    @Test
    void obtenerPerfilEmpresa_OK() throws Exception {
    }

    // 9) obtenerPerfilEmpresa_NoEncontrado_Error: obtener id inexistente lanza IllegalArgumentException.
    @Test
    void obtenerPerfilEmpresa_NoEncontrado_Error() throws Exception {
    }

    // 10) traerTodoPerfilEmpresa_OK: devuelve lista de perfiles (>= los creados).
    @Test
    void traerTodoPerfilEmpresa_OK() throws Exception {
    }

    // 11) eliminarEmpresa_EliminaCorrectamente: elimina perfil empresa y verifica ausencia en repo.
    @Test
    void eliminarEmpresa_EliminaCorrectamente() throws Exception {
    }

    // 12) cascadaEliminar_DireccionesTelefonos_SeEliminan: al eliminar perfil empresa se eliminan direcciones/telefonos por cascade.
    @Test
    void cascadaEliminar_DireccionesTelefonos_SeEliminan() throws Exception {
    }
}
