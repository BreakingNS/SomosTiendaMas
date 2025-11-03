package com.breakingns.SomosTiendaMas.entidades.telefono.repository;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.TelefonoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelefonoUsuarioRepository extends JpaRepository<TelefonoUsuario, Long> {
    List<TelefonoUsuario> findByPerfilUsuarioId(Long perfilUsuarioId);
    // Nuevo: eliminación masiva por perfil de usuario
    void deleteByPerfilUsuarioId(Long perfilUsuarioId);
}
