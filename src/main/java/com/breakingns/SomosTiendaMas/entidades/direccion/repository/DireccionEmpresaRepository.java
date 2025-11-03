package com.breakingns.SomosTiendaMas.entidades.direccion.repository;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionEmpresaRepository extends JpaRepository<DireccionEmpresa, Long> {
    List<DireccionEmpresa> findByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);
    // Nuevo: eliminación masiva por perfil de empresa
    void deleteByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);
}
