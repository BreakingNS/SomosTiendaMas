package com.breakingns.SomosTiendaMas.test.Modulo2.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
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

import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ICodigoAreaRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.TelefonoService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/*
    TelefonoFeatureTest - casos recomendados

    1) crearTelefonoParaUsuarioYEmpresa_OK
    2) crearTelefono_CaracteristicaInvalida_Error
    3) crearTelefono_XOROwner_Error
    4) crearTelefono_EvitarDuplicadoMismoOwner_OK
    5) actualizarTelefono_Valido_OK
    6) listarTelefonosPorUsuario_OK
    7) listarTelefonosPorEmpresa_OK
    8) eliminarTelefonosPorUsuario_Elimina_OK
    9) traerTodoTelefono_E2E_ServiceToRepo
    10) obtenerTelefono_NoEncontrado_Error
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
    )
})
public class TelefonoTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final IUsuarioRepository usuarioRepository;
    private final IPerfilEmpresaRepository perfilEmpresaRepository;
    private final ITelefonoRepository telefonoRepository;
    private final TelefonoService telefonoService;
    private final IPaisRepository paisRepository;
    private final IProvinciaRepository provinciaRepository;

    @MockBean
    private ICodigoAreaRepository codigoAreaRepository;

    @MockBean
    private org.springframework.context.ApplicationEventPublisher eventPublisher; // si el registro dispara eventos (silencia)

    private RegistroUsuarioCompletoDTO registroDTO;

    @BeforeEach
    void setUp() throws Exception {
        // preparar usuario base (reutiliza el mismo patrón que DireccionTest)
        RegistroUsuarioDTO usuarioDTO = new RegistroUsuarioDTO();
        usuarioDTO.setUsername("usuarioTel");
        usuarioDTO.setEmail("correoempresa@noenviar.com");
        usuarioDTO.setPassword("ClaveSegura123");
        usuarioDTO.setNombreResponsable("Ana");
        usuarioDTO.setApellidoResponsable("Telefono");
        usuarioDTO.setDocumentoResponsable("55555555");
        usuarioDTO.setTipoUsuario("PERSONA_FISICA");
        usuarioDTO.setAceptaTerminos(true);
        usuarioDTO.setAceptaPoliticaPriv(true);
        usuarioDTO.setFechaNacimientoResponsable(LocalDate.of(1990,1,1));
        usuarioDTO.setGeneroResponsable("FEMENINO");
        usuarioDTO.setIdioma("es");
        usuarioDTO.setTimezone("America/Argentina/Buenos_Aires");
        usuarioDTO.setRol("ROLE_USUARIO");

        registroDTO = new RegistroUsuarioCompletoDTO();
        registroDTO.setUsuario(usuarioDTO);
        registroDTO.setDirecciones(List.of());
        registroDTO.setTelefonos(List.of());

        // registrar vía endpoint de gestion usuario (igual que DireccionTest)
        MvcResult r = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/gestionusuario/public/usuario/registro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registroDTO)))
            .andReturn();

        // asegurar usuario creado y marcado email verificado para evitar bloqueos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername("usuarioTel");
        assertTrue(usuarioOpt.isPresent());
        Usuario usuario = usuarioOpt.get();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
         
        // MOCK: aceptar características (códigos de área) comunes usadas en tests
        Mockito.when(codigoAreaRepository.findByCodigo(Mockito.anyString()))
            .thenAnswer(inv -> Optional.of(Mockito.mock(com.breakingns.SomosTiendaMas.entidades.telefono.model.CodigoArea.class)));

    }

    // helper: crear DTO básico para usuario
    private RegistroTelefonoDTO crearTelefonoUsuarioDto(Usuario usuario, String tipo, String caracteristica, String numero, Boolean activo, Boolean verificado) {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setTipo(tipo);
        dto.setCaracteristica(caracteristica);
        dto.setNumero(numero);
        dto.setActivo(activo);
        dto.setVerificado(verificado);
        dto.setIdPerfilEmpresa(null);
        return dto;
    }

    // helper: crear DTO básico para empresa
    private RegistroTelefonoDTO crearTelefonoEmpresaDto(Long idPerfilEmpresa, String tipo, String caracteristica, String numero) {
        RegistroTelefonoDTO dto = new RegistroTelefonoDTO();
        dto.setIdPerfilEmpresa(idPerfilEmpresa);
        dto.setTipo(tipo);
        dto.setCaracteristica(caracteristica);
        dto.setNumero(numero);
        dto.setActivo(true);
        dto.setVerificado(false);
        dto.setIdUsuario(null);
        return dto;
    }

    // 1) crear teléfono para usuario y empresa OK.
    @Test
    void crearTelefonoParaUsuarioYEmpresa_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();

        RegistroTelefonoDTO t1 = crearTelefonoUsuarioDto(usuario, "PRINCIPAL", "11", "4123456", true, false);
        var resp1 = telefonoService.registrarTelefono(t1);
        assertTrue(resp1.getIdTelefono() != null);

        // crear perfil empresa
        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("TEL S.A.");
        perfil.setCuit("30711111119");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoempresa@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroTelefonoDTO t2 = crearTelefonoEmpresaDto(saved.getIdPerfilEmpresa(), "EMPRESA", "381", "4987654");
        var resp2 = telefonoService.registrarTelefono(t2);
        assertTrue(resp2.getIdTelefono() != null);

        // comprobar persistencia by repo
        List<Telefono> all = telefonoRepository.findAll();
        assertTrue(all.stream().anyMatch(x -> x.getUsuario()!=null && x.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())));
        assertTrue(all.stream().anyMatch(x -> x.getPerfilEmpresa()!=null && x.getPerfilEmpresa().getIdPerfilEmpresa().equals(saved.getIdPerfilEmpresa())));
    }

    // 2) Crear teléfono con característica inválida -> error.
    @Test
    void crearTelefono_CaracteristicaInvalida_Error() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        // MOCK: devolver empty para caracteristica inválida
        Mockito.when(codigoAreaRepository.findByCodigo("999")).thenReturn(Optional.empty());

        RegistroTelefonoDTO bad = crearTelefonoUsuarioDto(usuario, "PRINCIPAL", "999", "0000", true, false);
        assertThrows(IllegalArgumentException.class, () -> telefonoService.registrarTelefono(bad));
    }
    
    // 3) Crear teléfono: XOR owner (ambos o ninguno) -> error.
    @Test
    void crearTelefono_XOROwner_Error() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();

        // ninguno
        RegistroTelefonoDTO none = new RegistroTelefonoDTO();
        none.setTipo("PRINCIPAL");
        none.setCaracteristica("11");
        none.setNumero("1234");
        none.setActivo(true);
        none.setVerificado(false);
        assertThrows(IllegalArgumentException.class, () -> telefonoService.registrarTelefono(none));

        // ambos
        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("XOR SA");
        perfil.setCuit("30722222228");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoempresa@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroTelefonoDTO both = new RegistroTelefonoDTO();
        both.setIdUsuario(usuario.getIdUsuario());
        both.setIdPerfilEmpresa(saved.getIdPerfilEmpresa());
        both.setTipo("PRINCIPAL");
        both.setCaracteristica("11");
        both.setNumero("5678");
        both.setActivo(true);
        both.setVerificado(false);
        assertThrows(IllegalArgumentException.class, () -> telefonoService.registrarTelefono(both));
    }

    // 4) Evitar duplicado de teléfono para el mismo owner.
    @Test
    void crearTelefono_EvitarDuplicadoMismoOwner_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        RegistroTelefonoDTO t = crearTelefonoUsuarioDto(usuario, "PRINCIPAL", "11", "77777777", true, false);

        var r1 = telefonoService.registrarTelefono(t);
        var r2 = telefonoService.registrarTelefono(t);

        assertEquals(r1.getNumero(), r2.getNumero());
        assertEquals(r1.getIdTelefono(), r2.getIdTelefono(), "Si es duplicado debe devolverse el existente");
    }

    // 5) Actualizar teléfono válido OK.
    @Test
    void actualizarTelefono_Valido_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        RegistroTelefonoDTO t = crearTelefonoUsuarioDto(usuario, "PRINCIPAL", "11", "66666666", true, false);
        var created = telefonoService.registrarTelefono(t);

        ActualizarTelefonoDTO upd = new ActualizarTelefonoDTO();
        upd.setTipo("SECUNDARIO");
        upd.setNumero("66660000");
        upd.setCaracteristica("11");
        upd.setActivo(false);
        upd.setVerificado(true);

        var resp = telefonoService.actualizarTelefono(created.getIdTelefono(), upd);
        assertEquals("66660000", resp.getNumero());
        assertEquals("SECUNDARIO", resp.getTipo());
        assertFalse(resp.getActivo());
        assertTrue(resp.getVerificado());
    }

    // 6) Listar teléfonos por usuario OK.
    @Test
    void listarTelefonosPorUsuario_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        // asegurar al menos uno
        RegistroTelefonoDTO t = crearTelefonoUsuarioDto(usuario, "WHATSAPP", "11", "55112233", true, true);
        telefonoService.registrarTelefono(t);

        List<com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO> list = telefonoService.listarTelefonosPorUsuario(usuario.getIdUsuario());
        assertTrue(list.size() >= 1);
        assertTrue(list.stream().anyMatch(x -> "55112233".equals(x.getNumero())));
    }

    // 7) Listar teléfonos por empresa OK.
    @Test
    void listarTelefonosPorEmpresa_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        PerfilEmpresa perfil = new PerfilEmpresa();
        perfil.setUsuario(usuario);
        perfil.setRazonSocial("TEL LIST");
        perfil.setCuit("30733333337");
        perfil.setCondicionIVA(PerfilEmpresa.CondicionIVA.RI);
        perfil.setEmailEmpresa("correoempresa@noenviar.com");
        perfil.setRequiereFacturacion(true);
        perfil.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.APROBADO);
        perfil.setFechaCreacion(java.time.LocalDateTime.now());
        perfil.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfil.setActivo(true);
        PerfilEmpresa saved = perfilEmpresaRepository.save(perfil);

        RegistroTelefonoDTO t = crearTelefonoEmpresaDto(saved.getIdPerfilEmpresa(), "EMPRESA", "381", "381000111");
        telefonoService.registrarTelefono(t);

        List<com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO> list = telefonoService.listarTelefonosPorPerfilEmpresa(saved.getIdPerfilEmpresa());
        assertTrue(list.size() >= 1);
        assertTrue(list.stream().anyMatch(x -> "381000111".equals(x.getNumero())));
    }

    // 8) Eliminar teléfonos por usuario -> se eliminan.
    @Test
    void eliminarTelefonosPorUsuario_Elimina_OK() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        RegistroTelefonoDTO t = crearTelefonoUsuarioDto(usuario, "PRINCIPAL", "11", "99990000", true, false);
        telefonoService.registrarTelefono(t);

        List<Telefono> before = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertTrue(before.size() >= 1);

        telefonoService.eliminarTelefonosPorUsuario(usuario.getIdUsuario());
        List<Telefono> after = telefonoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        assertEquals(0, after.size());
    }

    // 9) E2E: traer todo teléfono / service -> repo
    @Test
    void traerTodoTelefono_E2E_ServiceToRepo() {
        Usuario usuario = usuarioRepository.findByUsername("usuarioTel").orElseThrow();
        RegistroTelefonoDTO t = crearTelefonoUsuarioDto(usuario, "SECUNDARIO", "11", "44443333", true, false);
        telefonoService.registrarTelefono(t);

        List<Telefono> all = telefonoService.traerTodoTelefono();
        assertTrue(all.size() >= 1);
        assertTrue(all.stream().anyMatch(x -> "44443333".equals(x.getNumero())));
    }

    // 10) obtenerTelefono_NoEncontrado -> error
    @Test
    void obtenerTelefono_NoEncontrado_Error() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
            () -> telefonoService.obtenerTelefono(9_999_999L));
    }
}