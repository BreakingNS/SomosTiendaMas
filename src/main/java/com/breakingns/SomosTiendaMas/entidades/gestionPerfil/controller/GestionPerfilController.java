package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.controller;

import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.auth.service.EmailVerificacionService;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.DireccionService;
import com.breakingns.SomosTiendaMas.entidades.empresa.service.PerfilEmpresaService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.ActualizarEmpresaCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.ActualizarUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.ConsultaEmpresaCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.ConsultaUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroEmpresaCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.TelefonoService;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.utils.RequestUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/gestionusuario")
public class GestionPerfilController {
    
    private final UsuarioServiceImpl usuarioService;
    private final PerfilEmpresaService perfilEmpresaService;
    private final DireccionService direccionService;
    private final TelefonoService telefonoService;
    private final EmailVerificacionService emailVerificacionService;
    private final EmailService emailService;

    public GestionPerfilController(UsuarioServiceImpl usuarioService, 
                                PerfilEmpresaService perfilEmpresaService,
                                DireccionService direccionService,
                                TelefonoService telefonoService,
                                EmailVerificacionService emailVerificacionService,
                                EmailService emailService) {
        this.usuarioService = usuarioService;
        this.perfilEmpresaService = perfilEmpresaService;
        this.direccionService = direccionService;
        this.telefonoService = telefonoService;
        this.emailVerificacionService = emailVerificacionService;
        this.emailService = emailService;
    }
    
    /*                                   Endpoints:
        Endpoints Usuario:
            
            1. /public/usuario/registro         -> registrarUsuario
            2. /public/usuario/edicion/{id}     -> editarUsuario
            3. /public/usuario/consulta/{id}    -> consultarUsuario
            4. /public/usuario/eliminar/{id}    -> eliminarUsuario

        Endpoints Empresa:

            1. /public/empresa/registro         -> registrarEmpresa
            2. /public/empresa/edicion/{id}     -> editarEmpresa
            3. /public/empresa/consulta/{id}    -> consultarEmpresa
            4. /public/empresa/eliminar/{id}    -> eliminarEmpresa
        
    */

    private static final Set<String> EMAILS_BLOQUEADOS = Set.of(
        "correoprueba@noenviar.com",
        "correoprueba1@noenviar.com",
        "correoprueba2@noenviar.com",
        "correoempresa@noenviar.com",
        "correoempresa1@noenviar.com",
        "correoempresa2@noenviar.com"
    );

    @PostMapping("/public/usuario/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody @Valid RegistroUsuarioCompletoDTO dto, HttpServletRequest request) {
        String ip = RequestUtil.obtenerIpCliente(request);
        // 1. Registrar usuario
        Long idUsuario = usuarioService.registrarConRolDesdeDTO(dto.getUsuario(), ip);
        // 2. Registrar direcciones
        if (dto.getDirecciones() != null) {
            dto.getDirecciones().forEach(direccion -> {
                direccion.setIdUsuario(idUsuario);
                direccionService.registrarDireccion(direccion);
            });
        }
        // 3. Registrar teléfonos
        if (dto.getTelefonos() != null) {
            dto.getTelefonos().forEach(telefono -> {
                telefono.setIdUsuario(idUsuario);
                telefonoService.registrarTelefono(telefono);
            });
        }
        // Generar código de verificación y enviar email
        Usuario usu = usuarioService.findById(idUsuario).orElseThrow(() -> new RuntimeException("Usuario no encontrado")); 
        EmailVerificacion verificacion = emailVerificacionService.generarCodigoParaUsuario(usu);
        if (!EMAILS_BLOQUEADOS.contains(usu.getEmail())) {
            emailService.enviarEmailVerificacion(usu.getEmail(), verificacion.getCodigo());
        }
        
        return ResponseEntity.ok("Usuario registrado correctamente. Verifica tu email.");
    }

    @PutMapping("/public/usuario/edicion/{id}")
    public ResponseEntity<String> editarUsuario(@PathVariable Long id, @RequestBody @Valid ActualizarUsuarioCompletoDTO dto) {
        // 1. Editar usuario
        usuarioService.actualizarUsuario(id, dto.getUsuario(), id);
        // 2. Editar direcciones
        if (dto.getDirecciones() != null) {
            dto.getDirecciones().forEach(direccion -> {
                direccionService.actualizarDireccion(id, direccion);
            });
        }
        // 3. Editar teléfonos
        if (dto.getTelefonos() != null) {
            dto.getTelefonos().forEach(telefono -> {
                telefonoService.actualizarTelefono(id, telefono);
            });
        }
        return ResponseEntity.ok("Usuario actualizado correctamente.");
    }

    @GetMapping("/public/usuario/consulta/{id}")
    public ResponseEntity<ConsultaUsuarioCompletoDTO> consultarUsuario(@PathVariable Long id) {
        // 1. Consultar usuario
        var usuario = usuarioService.consultarUsuario(id);
        // 2. Consultar direcciones
        var direcciones = direccionService.listarDireccionesPorUsuario(id);
        // 3. Consultar teléfonos
        var telefonos = telefonoService.listarTelefonosPorUsuario(id);
        // 4. Armar DTO completo
        ConsultaUsuarioCompletoDTO dto = new ConsultaUsuarioCompletoDTO();
        dto.setUsuario(usuario);
        dto.setDirecciones(direcciones);
        dto.setTelefonos(telefonos);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/public/usuario/eliminar/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        // 1. Eliminar teléfonos
        telefonoService.eliminarTelefonosPorUsuario(id);
        // 2. Eliminar direcciones
        direccionService.eliminarDireccionesPorUsuario(id);
        // 3. Eliminar usuario
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }

    @PostMapping("/public/empresa/registro")
    public ResponseEntity<String> registrarEmpresa(@RequestBody @Valid RegistroEmpresaCompletoDTO dto, HttpServletRequest request) {
        String ip = RequestUtil.obtenerIpCliente(request);
        // 1. Registrar responsable
        Long idUsuario = usuarioService.registrarConRolDesdeDTO(dto.getResponsable(), ip);
        // 2. Registrar direcciones y teléfonos de responsable
        if (dto.getDireccionesResponsable() != null) {
            dto.getDireccionesResponsable().forEach(direccion -> {
                direccion.setIdUsuario(idUsuario);
                direccionService.registrarDireccion(direccion);
            });
        }
        if (dto.getTelefonosResponsable() != null) {
            dto.getTelefonosResponsable().forEach(telefono -> {
                telefono.setIdUsuario(idUsuario);
                telefonoService.registrarTelefono(telefono);
            });
        }
        // 3. Registrar perfil empresa
        final Long idPerfilEmpresa;
        if (dto.getPerfilEmpresa() != null) {
            dto.getPerfilEmpresa().setIdUsuario(idUsuario);
            idPerfilEmpresa = perfilEmpresaService.registrarPerfilEmpresa(dto.getPerfilEmpresa()).getId();
        } else {
            idPerfilEmpresa = null;
        }
        // 4. Registrar direcciones y teléfonos de la empresa
        if (dto.getDireccionesEmpresa() != null && idPerfilEmpresa != null) {
            dto.getDireccionesEmpresa().forEach(direccion -> {
                direccion.setIdPerfilEmpresa(idPerfilEmpresa);
                direccionService.registrarDireccion(direccion);
            });
        }
        if (dto.getTelefonosEmpresa() != null && idPerfilEmpresa != null) {
            dto.getTelefonosEmpresa().forEach(telefono -> {
                telefono.setIdPerfilEmpresa(idPerfilEmpresa);
                telefonoService.registrarTelefono(telefono);
            });
        }
        // Generar código de verificación y enviar email
        Usuario usuario = usuarioService.findById(idUsuario).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        EmailVerificacion verificacion = emailVerificacionService.generarCodigoParaUsuario(usuario);
        emailService.enviarEmailVerificacion(usuario.getEmail(), verificacion.getCodigo());

        return ResponseEntity.ok("Usuario registrado correctamente. Verifica tu email.");
    }

    @PutMapping("/public/empresa/edicion/{id}")
    public ResponseEntity<String> editarEmpresa(@PathVariable Long id, @RequestBody @Valid ActualizarEmpresaCompletoDTO dto) {
        // 1. Editar responsable
        usuarioService.actualizarUsuario(dto.getResponsable().getUsername() != null ? id : null, dto.getResponsable(), id);
        // 2. Editar perfil empresa
        if (dto.getPerfilEmpresa() != null) {
            perfilEmpresaService.actualizarPerfilEmpresa(id, dto.getPerfilEmpresa());
        }
        // 3. Editar direcciones y teléfonos del responsable
        if (dto.getDireccionesResponsable() != null) {
            dto.getDireccionesResponsable().forEach(direccion -> {
                direccionService.actualizarDireccion(id, direccion);
            });
        }
        if (dto.getTelefonosResponsable() != null) {
            dto.getTelefonosResponsable().forEach(telefono -> {
                telefonoService.actualizarTelefono(id, telefono);
            });
        }
        // 4. Editar direcciones y teléfonos de la empresa
        if (dto.getDireccionesEmpresa() != null) {
            dto.getDireccionesEmpresa().forEach(direccion -> {
                direccionService.actualizarDireccion(id, direccion);
            });
        }
        if (dto.getTelefonosEmpresa() != null) {
            dto.getTelefonosEmpresa().forEach(telefono -> {
                telefonoService.actualizarTelefono(id, telefono);
            });
        }
        return ResponseEntity.ok("Empresa actualizada correctamente.");
    }

    @GetMapping("/public/empresa/consulta/{id}")
    public ResponseEntity<ConsultaEmpresaCompletoDTO> consultarEmpresa(@PathVariable Long id) {
        // 1. Consultar perfil empresa
        var perfil = perfilEmpresaService.obtenerPerfilEmpresa(id);
        // 2. Consultar responsable
        var responsable = usuarioService.consultarUsuario(perfil.getIdUsuario());
        // 3. Direcciones y teléfonos del responsable
        var direccionesResponsable = direccionService.listarDireccionesPorUsuario(perfil.getIdUsuario());
        var telefonosResponsable = telefonoService.listarTelefonosPorUsuario(perfil.getIdUsuario());
        // 4. Direcciones y teléfonos de la empresa
        var direccionesEmpresa = direccionService.listarDireccionesPorPerfilEmpresa(id);
        var telefonosEmpresa = telefonoService.listarTelefonosPorPerfilEmpresa(id);
        // 5. Armar DTO completo
        ConsultaEmpresaCompletoDTO dto = new ConsultaEmpresaCompletoDTO();
        dto.setResponsable(responsable);
        dto.setPerfilEmpresa(perfil);
        dto.setDireccionesResponsable(direccionesResponsable);
        dto.setTelefonosResponsable(telefonosResponsable);
        dto.setDireccionesEmpresa(direccionesEmpresa);
        dto.setTelefonosEmpresa(telefonosEmpresa);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("public/empresa/eliminar/{id}")
    public ResponseEntity<String> eliminarEmpresa(@PathVariable Long id) {
        // 1. Consultar perfil empresa para obtener el id del responsable
        var perfil = perfilEmpresaService.obtenerPerfilEmpresa(id);
        Long idUsuario = perfil.getIdUsuario();
        // 2. Eliminar teléfonos y direcciones del responsable
        telefonoService.eliminarTelefonosPorUsuario(idUsuario);
        direccionService.eliminarDireccionesPorUsuario(idUsuario);
        // 3. Eliminar teléfonos y direcciones de la empresa
        telefonoService.eliminarTelefonosPorPerfilEmpresa(id);
        direccionService.eliminarDireccionesPorPerfilEmpresa(id);
        // 4. Eliminar perfil empresa
        perfilEmpresaService.eliminarEmpresa(id);
        return ResponseEntity.ok("Empresa eliminada correctamente.");
    }
    
    @PostMapping("/public/verificar-email")
    public ResponseEntity<String> verificarEmail(@RequestBody Map<String, String> body) {
        String codigo = body.get("codigo");
        emailVerificacionService.verificarCodigo(codigo);
        return ResponseEntity.ok("Email verificado correctamente.");
    }

    @PatchMapping("/private/usuario/desactivar/{id}")
        public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
