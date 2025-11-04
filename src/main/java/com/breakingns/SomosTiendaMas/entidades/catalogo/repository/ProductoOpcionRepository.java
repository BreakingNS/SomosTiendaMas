package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoOpcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoOpcionRepository extends JpaRepository<ProductoOpcion, Long> {

    List<ProductoOpcion> findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    Optional<ProductoOpcion> findByProducto_IdAndOpcion_IdAndDeletedAtIsNull(Long productoId, Long opcionId);

    boolean existsByProducto_IdAndOpcion_IdAndDeletedAtIsNull(Long productoId, Long opcionId);

    @Query("select max(p.orden) from ProductoOpcion p where p.producto.id = :productoId and p.deletedAt is null")
    Optional<Integer> findMaxOrdenByProductoId(@Param("productoId") Long productoId);
}
