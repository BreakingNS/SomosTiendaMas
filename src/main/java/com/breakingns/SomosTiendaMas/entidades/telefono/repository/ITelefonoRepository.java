package com.breakingns.SomosTiendaMas.entidades.telefono.repository;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ITelefonoRepository extends JpaRepository<Telefono, Long> {
    List<Telefono> findByUsuario_IdUsuario(Long idUsuario);
    List<Telefono> findByPerfilEmpresa_Id(Long idPerfilEmpresa);
}