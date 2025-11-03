package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.service;

import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.registrarDTO.PerfilUsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto.PerfilUsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.PerfilUsuario;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.repository.PerfilUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PerfilUsuarioService implements IPerfilUsuarioService {

    private final PerfilUsuarioRepository repo;

    public PerfilUsuarioService(PerfilUsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public PerfilUsuarioResponseDTO crearOActualizarPerfil(Usuario usuario, PerfilUsuarioCreateDTO dto) {
        PerfilUsuario perfil = repo.findByUsuario(usuario).orElseGet(() -> {
            PerfilUsuario p = new PerfilUsuario();
            p.setUsuario(usuario);
            System.out.println("Creando nuevo perfil para usuario ID: " + usuario.getIdUsuario() + "\n\n");
            return p;
        });

        if (dto.getNombre() != null) perfil.setNombre(dto.getNombre());
        if (dto.getApellido() != null) perfil.setApellido(dto.getApellido());
        if (dto.getDocumento() != null) perfil.setDocumento(dto.getDocumento());
        if (dto.getFechaNacimiento() != null) perfil.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getGenero() != null) perfil.setGenero(dto.getGenero());
        if (dto.getCargo() != null) perfil.setCargo(dto.getCargo());
        if (dto.getCorreoAlternativo() != null) perfil.setCorreoAlternativo(dto.getCorreoAlternativo());

        PerfilUsuario saved = repo.save(perfil);

        return new PerfilUsuarioResponseDTO(
                saved.getId(),
                saved.getUsuario().getIdUsuario(),
                saved.getNombre(),
                saved.getApellido(),
                saved.getDocumento(),
                saved.getFechaNacimiento(),
                saved.getGenero(),
                saved.getCargo(),
                saved.getCorreoAlternativo()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PerfilUsuarioResponseDTO> obtenerPorUsuario(Usuario usuario) {
        return repo.findByUsuario(usuario).map(p ->
                new PerfilUsuarioResponseDTO(
                        p.getId(),
                        p.getUsuario().getIdUsuario(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getDocumento(),
                        p.getFechaNacimiento(),
                        p.getGenero(),
                        p.getCargo(),
                        p.getCorreoAlternativo()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PerfilUsuarioResponseDTO> obtenerPorUsuarioId(Long usuarioId) {
        return repo.findByUsuario_IdUsuario(usuarioId).map(p ->
                new PerfilUsuarioResponseDTO(
                        p.getId(),
                        p.getUsuario().getIdUsuario(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getDocumento(),
                        p.getFechaNacimiento(),
                        p.getGenero(),
                        p.getCargo(),
                        p.getCorreoAlternativo()
                )
        );
    }
}
