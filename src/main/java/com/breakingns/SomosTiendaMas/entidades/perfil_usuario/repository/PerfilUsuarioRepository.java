package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.repository;

import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.PerfilUsuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Long> {
    Optional<PerfilUsuario> findByUsuario(Usuario usuario);
    Optional<PerfilUsuario> findByUsuario_IdUsuario(Long usuarioId);
}
