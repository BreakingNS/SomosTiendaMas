package com.breakingns.SomosTiendaMas.entidades.telefono.repository;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.TelefonoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelefonoEmpresaRepository extends JpaRepository<TelefonoEmpresa, Long> {
    List<TelefonoEmpresa> findByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);
    // Nuevo: eliminación masiva por perfil de empresa
    void deleteByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);
}
