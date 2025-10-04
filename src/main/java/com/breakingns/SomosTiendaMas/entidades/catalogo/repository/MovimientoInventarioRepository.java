package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    Optional<MovimientoInventario> findFirstByOrderRefOrderByCreatedAtAsc(String orderRef);
    List<MovimientoInventario> findByOrderRef(String orderRef);
}
