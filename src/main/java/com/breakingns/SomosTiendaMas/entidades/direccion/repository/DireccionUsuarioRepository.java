package com.breakingns.SomosTiendaMas.entidades.direccion.repository;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionUsuarioRepository extends JpaRepository<DireccionUsuario, Long> {
    List<DireccionUsuario> findByPerfilUsuarioId(Long perfilUsuarioId);
    // Nuevo: eliminación masiva por perfil de usuario
    void deleteByPerfilUsuarioId(Long perfilUsuarioId);
}
