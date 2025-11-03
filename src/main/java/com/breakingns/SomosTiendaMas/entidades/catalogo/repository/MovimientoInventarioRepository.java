package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    Optional<MovimientoInventario> findFirstByOrderRefOrderByCreatedAtAsc(String orderRef);
    List<MovimientoInventario> findByOrderRef(String orderRef);

    // corregidos: usar el nombre real del campo en la entidad -> 'tipo'
    Optional<MovimientoInventario> findFirstByOrderRefAndTipoOrderByCreatedAtAsc(String orderRef, TipoMovimientoInventario tipo);
    List<MovimientoInventario> findByProducto_Id(Long productoId);
    List<MovimientoInventario> findByOrderRefAndTipo(String orderRef, TipoMovimientoInventario tipo);
}