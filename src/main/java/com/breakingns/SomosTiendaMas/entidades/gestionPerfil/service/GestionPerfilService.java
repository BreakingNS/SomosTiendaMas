package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.service;

import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import com.breakingns.SomosTiendaMas.auth.service.EmailService;
import com.breakingns.SomosTiendaMas.auth.service.EmailVerificacionService;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.IDireccionService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.registrarDTO.PerfilUsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.service.IPerfilEmpresaService;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto.PerfilUsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.service.IPerfilUsuarioService;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ITelefonoService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.IUsuarioService;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroEmpresaCompletoDTO;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.RegistroUsuarioCompletoDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class GestionPerfilService {

    private final IUsuarioService usuarioService;
    private final IPerfilUsuarioService perfilUsuarioService;
    private final IPerfilEmpresaService perfilEmpresaService;
    private final IDireccionService direccionService;
    private final ITelefonoService telefonoService;
    private final EmailVerificacionService emailVerificacionService;
    private final EmailService emailService;

    public GestionPerfilService(
            IUsuarioService usuarioService,
            IPerfilUsuarioService perfilUsuarioService,
            IPerfilEmpresaService perfilEmpresaService,
            IDireccionService direccionService,
            ITelefonoService telefonoService,
            EmailVerificacionService emailVerificacionService,
            EmailService emailService) {
        this.usuarioService = usuarioService;
        this.perfilUsuarioService = perfilUsuarioService;
        this.perfilEmpresaService = perfilEmpresaService;
        this.direccionService = direccionService;
        this.telefonoService = telefonoService;
        this.emailVerificacionService = emailVerificacionService;
        this.emailService = emailService;
    }

    public static class RegistroResult {
        public final Long idUsuario;
        public final Long idPerfilUsuario;
        public RegistroResult(Long idUsuario, Long idPerfilUsuario) {
            this.idUsuario = idUsuario;
            this.idPerfilUsuario = idPerfilUsuario;
        }
    }

    @Transactional
    public RegistroResult registrarUsuarioCompleto(RegistroUsuarioCompletoDTO dto, String clientIp) {
        // 1) Registrar Usuario (delegate)
        RegistroUsuarioDTO regUsuarioDto = dto.getUsuario();
        Long idUsuario = usuarioService.registrarConRolDesdeDTO(regUsuarioDto, clientIp);

        System.out.println("Usuario registrado con ID: " + idUsuario + "\n\n");

        Usuario usuario = usuarioService.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Registrar PerfilUsuario (FK -> Usuario)
        Long idPerfilUsuario = null;
        if (dto.getPerfilUsuario() != null) {
            // dto.getPerfilUsuario() es PerfilUsuarioCreateDTO (sin usuarioId)
            PerfilUsuarioCreateDTO perfilCreate = dto.getPerfilUsuario();
            PerfilUsuarioResponseDTO perfilResp = perfilUsuarioService.crearOActualizarPerfil(usuario, perfilCreate);
            idPerfilUsuario = perfilResp.getId();
        }

        // 3) Registrar Direcciones (FK -> PerfilUsuario)
        if (dto.getDirecciones() != null && idPerfilUsuario != null) {
            final Long fkPerfil = idPerfilUsuario;
            dto.getDirecciones().forEach(d -> {
                d.setPerfilUsuarioId(fkPerfil);
                direccionService.registrarDireccion(d);
            });
        }

        // 4) Registrar Teléfonos (FK -> PerfilUsuario)
        if (dto.getTelefonos() != null && idPerfilUsuario != null) {
            final Long fkPerfil = idPerfilUsuario;
            dto.getTelefonos().forEach(t -> {
                t.setPerfilUsuarioId(fkPerfil);
                telefonoService.registrarTelefono(t);
            });
        }

        // 5) Generar código de verificación para el usuario dentro de la tx
        EmailVerificacion verificacion = emailVerificacionService.generarCodigoParaUsuario(usuario);

        // Registrar envío de email para afterCommit (evita mails si la TX falla)
        if (emailService != null && verificacion != null && usuario.getEmail() != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        emailService.enviarEmailVerificacion(usuario.getEmail(), verificacion.getCodigo());
                    } catch (Exception ex) {
                        // loguear en tu logger (no lanzar)
                        ex.printStackTrace();
                    }
                }
            });
        }

        return new RegistroResult(idUsuario, idPerfilUsuario);
    }

    public static class RegistroEmpresaResult {
        public final Long idUsuario;
        public final Long idPerfilUsuario;
        public final Long idPerfilEmpresa;
        public RegistroEmpresaResult(Long u, Long pU, Long pE) {
            this.idUsuario = u; this.idPerfilUsuario = pU; this.idPerfilEmpresa = pE;
        }
    }

    @Transactional
    public RegistroEmpresaResult registrarEmpresaConResponsable(RegistroEmpresaCompletoDTO dto, String clientIp) {
        // 1) Validaciones previas (username/email/cuit/doc)
        if (usuarioService.existsByUsername(dto.getUsuario().getUsername())) {
            throw new IllegalArgumentException("username ya existe");
        }
        if (usuarioService.existsByEmail(dto.getUsuario().getEmail())) {
            throw new IllegalArgumentException("email ya registrado");
        }
        if (dto.getPerfilEmpresa() != null && perfilEmpresaService.existsByCuit(dto.getPerfilEmpresa().getCuit())) {
            throw new IllegalArgumentException("empresa con ese CUIT ya existe");
        }

        // 2) Crear Usuario
        Long idUsuario = usuarioService.registrarConRolDesdeDTO(dto.getUsuario(), clientIp);
        Usuario usuario = usuarioService.findById(idUsuario).orElseThrow();

        // 3) Crear PerfilUsuario (responsable) y vincular FK usuario
        PerfilUsuarioCreateDTO perfilUserDto = dto.getPerfilUsuario();
        PerfilUsuarioResponseDTO perfilUserResp = perfilUsuarioService.crearOActualizarPerfil(usuario, perfilUserDto);
        Long idPerfilUsuario = perfilUserResp.getId();

        // 4) Crear PerfilEmpresa vinculando usuario y responsable
        RegistroPerfilEmpresaDTO empresaDto = dto.getPerfilEmpresa();
        empresaDto.setIdUsuario(idUsuario); // si tu DTO lo necesita
        empresaDto.setResponsablePerfilId(idPerfilUsuario);
        PerfilEmpresaResponseDTO empresaResp = perfilEmpresaService.registrarPerfilEmpresa(empresaDto);
        Long idPerfilEmpresa = empresaResp.getId();

        // 5) Direcciones y teléfonos (usuario/responsable)
        /*
        if (dto.getDireccionesResponsable() != null) {
            dto.getDireccionesResponsable().forEach(d -> {
                d.setPerfilUsuarioId(idPerfilUsuario);
                direccionService.registrarDireccion(d);
            });
        } */
        if (dto.getDireccionesEmpresa() != null) {
            dto.getDireccionesEmpresa().forEach(d -> {
                d.setPerfilEmpresaId(idPerfilEmpresa);
                direccionService.registrarDireccion(d);
            });
        }
        if (dto.getTelefonosResponsable() != null) {
            dto.getTelefonosResponsable().forEach(t -> {
                t.setPerfilUsuarioId(idPerfilUsuario);
                telefonoService.registrarTelefono(t);
            });
        }
        if (dto.getTelefonosEmpresa() != null) {
            dto.getTelefonosEmpresa().forEach(t -> {
                t.setPerfilEmpresaId(idPerfilEmpresa);
                telefonoService.registrarTelefono(t);
            });
        }

        // 6) Generar verificación y programar envío AFTER COMMIT
        EmailVerificacion ev = emailVerificacionService.generarCodigoParaUsuario(usuario);
        if (ev != null && usuario.getEmail() != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    emailService.enviarEmailVerificacion(usuario.getEmail(), ev.getCodigo());
                }
            });
        }

        return new RegistroEmpresaResult(idUsuario, idPerfilUsuario, idPerfilEmpresa);
    }
}