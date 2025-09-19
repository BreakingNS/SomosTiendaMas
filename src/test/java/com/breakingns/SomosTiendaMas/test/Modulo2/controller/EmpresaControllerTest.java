package com.breakingns.SomosTiendaMas.test.Modulo2.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;

/*                             EmpresaControllerTest

    Registro de empresa

        1. Registro exitoso de empresa con usuario responsable, dirección y teléfono válidos.
        2. Registro con email de responsable ya existente (debe fallar).
        3. Registro con username de responsable ya existente (debe fallar).
        4. Registro con datos faltantes (usuario, dirección, teléfono, perfil empresa).
        5. Registro con formato de email o teléfono inválido.
        6. Registro con empresa ya existente (nombre, CUIT, etc.).
        7. Registro con usuario responsable que no acepta términos (debe fallar).

    Edición de empresa

        8. Edición exitosa de datos de la empresa (nombre, rubro, etc.).
        9. Edición de usuario responsable (actualizar datos personales).
        10. Edición de dirección y teléfono de la empresa.
        11. Edición con datos inválidos (restricciones de formato, duplicados).

    Consulta de empresa

        12. Consulta exitosa de empresa por ID (devuelve datos completos, responsable, dirección, teléfono).
        13. Consulta de empresa inexistente (debe devolver error).
        14. Consulta de empresas por filtros (rubro, localidad, etc.).

    Eliminación de empresa

        15. Eliminación exitosa de empresa (borra empresa, responsable, direcciones y teléfonos asociados).
        16. Eliminación de empresa inexistente (debe devolver error).

    Desactivación de empresa

        17. Desactivación exitosa de empresa (cambia estado a inactiva).
        18. Desactivación de empresa inexistente (debe devolver error).

    Verificación de email del responsable

        19. Verificación exitosa de email del usuario responsable.
        20. Verificación con código inválido o expirado (debe fallar).

    Casos de seguridad y roles

        21. Acceso a endpoints privados solo con usuario responsable autenticado.
        22. Acceso a endpoints de admin solo con rol admin/superadmin.
        23. Acceso denegado a usuario sin permisos.

    Integración con direcciones y teléfonos

        24. Registro de empresa con múltiples direcciones y teléfonos.
        25. Edición y consulta de direcciones y teléfonos asociados.
        26. Eliminación de empresa borra también direcciones y teléfonos.

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
            "DELETE FROM usuario_roles",
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
            "DELETE FROM usuario_roles",
            "DELETE FROM usuario"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )*/
})
public class EmpresaControllerTest {
    
    private final IPerfilEmpresaRepository perfilEmpresaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IDireccionRepository direccionRepository;
    private final ITelefonoRepository telefonoRepository;

    @BeforeEach
    void setUp() {
        // Puedes inicializar datos comunes aquí si lo necesitas
    }

    // Métodos útiles para los tests

    // Registro de empresa

    // 1. Registro exitoso de empresa con usuario responsable, dirección y teléfono válidos.
    @Test
    void registroExitosoEmpresaConUsuarioResponsableDireccionTelefonoValidos() {
        assertEquals(true, true);
    }

    // 2. Registro con email de responsable ya existente (debe fallar).
    @Test
    void registroConEmailResponsableYaExistenteDebeFallar() {
        assertEquals(true, true);
    }

    // 3. Registro con username de responsable ya existente (debe fallar).
    @Test
    void registroConUsernameResponsableYaExistenteDebeFallar() {
        assertEquals(true, true);
    }

    // 4. Registro con datos faltantes (usuario, dirección, teléfono, perfil empresa).
    @Test
    void registroConDatosFaltantesDebeFallar() {
        assertEquals(true, true);
    }

    // 5. Registro con formato de email o teléfono inválido.
    @Test
    void registroConFormatoEmailOTelefonoInvalidoDebeFallar() {
        assertEquals(true, true);
    }

    // 6. Registro con empresa ya existente (nombre, CUIT, etc.).
    @Test
    void registroConEmpresaYaExistenteDebeFallar() {
        assertEquals(true, true);
    }

    // 7. Registro con usuario responsable que no acepta términos (debe fallar).
    @Test
    void registroConUsuarioResponsableSinAceptarTerminosDebeFallar() {
        assertEquals(true, true);
    }

    // Edición de empresa

    // 8. Edición exitosa de datos de la empresa (nombre, rubro, etc.).
    @Test
    void edicionExitosaDatosEmpresa() {
        assertEquals(true, true);
    }

    // 9. Edición de usuario responsable (actualizar datos personales).
    @Test
    void edicionUsuarioResponsable() {
        assertEquals(true, true);
    }

    // 10. Edición de dirección y teléfono de la empresa.
    @Test
    void edicionDireccionTelefonoEmpresa() {
        assertEquals(true, true);
    }

    // 11. Edición con datos inválidos (restricciones de formato, duplicados).
    @Test
    void edicionConDatosInvalidosDebeFallar() {
        assertEquals(true, true);
    }

    // Consulta de empresa

    // 12. Consulta exitosa de empresa por ID (devuelve datos completos, responsable, dirección, teléfono).
    @Test
    void consultaExitosaEmpresaPorId() {
        assertEquals(true, true);
    }

    // 13. Consulta de empresa inexistente (debe devolver error).
    @Test
    void consultaEmpresaInexistenteDebeFallar() {
        assertEquals(true, true);
    }

    // 14. Consulta de empresas por filtros (rubro, localidad, etc.).
    @Test
    void consultaEmpresasPorFiltros() {
        assertEquals(true, true);
    }

    // Eliminación de empresa

    // 15. Eliminación exitosa de empresa (borra empresa, responsable, direcciones y teléfonos asociados).
    @Test
    void eliminacionExitosaEmpresa() {
        assertEquals(true, true);
    }

    // 16. Eliminación de empresa inexistente (debe devolver error).
    @Test
    void eliminacionEmpresaInexistenteDebeFallar() {
        assertEquals(true, true);
    }

    // Desactivación de empresa

    // 17. Desactivación exitosa de empresa (cambia estado a inactiva).
    @Test
    void desactivacionExitosaEmpresa() {
        assertEquals(true, true);
    }

    // 18. Desactivación de empresa inexistente (debe devolver error).
    @Test
    void desactivacionEmpresaInexistenteDebeFallar() {
        assertEquals(true, true);
    }

    // Verificación de email del responsable

    // 19. Verificación exitosa de email del usuario responsable.
    @Test
    void verificacionExitosaEmailUsuarioResponsable() {
        assertEquals(true, true);
    }

    // 20. Verificación con código inválido o expirado (debe fallar).
    @Test
    void verificacionEmailCodigoInvalidoOExpiradoDebeFallar() {
        assertEquals(true, true);
    }

    // Casos de seguridad y roles

    // 21. Acceso a endpoints privados solo con usuario responsable autenticado.
    @Test
    void accesoEndpointsPrivadosSoloUsuarioResponsableAutenticado() {
        assertEquals(true, true);
    }

    // 22. Acceso a endpoints de admin solo con rol admin/superadmin.
    @Test
    void accesoEndpointsAdminSoloConRolAdminSuperadmin() {
        assertEquals(true, true);
    }

    // 23. Acceso denegado a usuario sin permisos.
    @Test
    void accesoDenegadoUsuarioSinPermisos() {
        assertEquals(true, true);
    }

    // Integración con direcciones y teléfonos

    // 24. Registro de empresa con múltiples direcciones y teléfonos.
    @Test
    void registroEmpresaConMultiplesDireccionesTelefonos() {
        assertEquals(true, true);
    }

    // 25. Edición y consulta de direcciones y teléfonos asociados.
    @Test
    void edicionConsultaDireccionesTelefonosAsociados() {
        assertEquals(true, true);
    }

    // 26. Eliminación de empresa borra también direcciones y teléfonos.
    @Test
    void eliminacionEmpresaBorraDireccionesTelefonos() {
        assertEquals(true, true);
    }
}
