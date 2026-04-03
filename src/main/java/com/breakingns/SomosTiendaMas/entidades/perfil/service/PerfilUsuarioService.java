package com.breakingns.SomosTiendaMas.entidades.perfil.service;

import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.registrarDTO.PerfilUsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil.dto.PerfilUsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.perfil.repository.PerfilRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PerfilUsuarioService implements IPerfilUsuarioService {

    private final PerfilRepository repo;

    public PerfilUsuarioService(PerfilRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public PerfilUsuarioResponseDTO crearOActualizarPerfil(Usuario usuario, PerfilUsuarioCreateDTO dto) {
        Perfil perfil = repo.findByUsuario(usuario).orElseGet(() -> {
            Perfil p = new Perfil();
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

        Perfil saved = repo.save(perfil);

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
