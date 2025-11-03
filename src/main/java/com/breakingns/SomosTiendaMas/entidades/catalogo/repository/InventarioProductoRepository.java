package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioProductoRepository extends JpaRepository<InventarioProducto, Long> {
    Optional<InventarioProducto> findByProducto(Producto producto);

    Optional<InventarioProducto> findByProductoId(Long productoId);
    Optional<InventarioProducto> findByProductoIdAndDeletedAtIsNull(Long productoId);

    List<InventarioProducto> findAllByDeletedAtIsNull();
    List<InventarioProducto> findByOnHandLessThan(Long threshold);
    List<InventarioProducto> findByReservedGreaterThan(Long threshold);

    // operaciones convenientes
    void deleteByProductoId(Long productoId);
}