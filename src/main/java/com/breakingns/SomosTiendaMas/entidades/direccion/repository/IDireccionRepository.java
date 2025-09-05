package com.breakingns.SomosTiendaMas.entidades.direccion.repository;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IDireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuario_IdUsuario(Long idUsuario);
    List<Direccion> findByPerfilEmpresa_Id(Long idPerfilEmpresa);
}
