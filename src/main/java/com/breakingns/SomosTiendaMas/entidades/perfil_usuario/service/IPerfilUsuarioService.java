package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.service;

import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.registrarDTO.PerfilUsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto.PerfilUsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import java.util.Optional;

public interface IPerfilUsuarioService {
    PerfilUsuarioResponseDTO crearOActualizarPerfil(Usuario usuario, PerfilUsuarioCreateDTO dto);
    Optional<PerfilUsuarioResponseDTO> obtenerPorUsuario(Usuario usuario);
    Optional<PerfilUsuarioResponseDTO> obtenerPorUsuarioId(Long usuarioId);
}
