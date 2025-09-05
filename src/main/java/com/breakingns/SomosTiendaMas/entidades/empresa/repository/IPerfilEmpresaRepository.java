package com.breakingns.SomosTiendaMas.entidades.empresa.repository;

import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IPerfilEmpresaRepository extends JpaRepository<PerfilEmpresa, Long> {
    Optional<PerfilEmpresa> findByCuit(String cuit);
    boolean existsByCuit(String cuit);
}
