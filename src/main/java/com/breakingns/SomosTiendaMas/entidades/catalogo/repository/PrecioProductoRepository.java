package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrecioProductoRepository extends JpaRepository<PrecioProducto, Long> {
    List<PrecioProducto> findByProductoIdOrderByVigenciaDesdeDesc(Long productoId);
    List<PrecioProducto> findByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(Long productoId);
    Optional<PrecioProducto> findFirstByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(Long productoId);

    // precio vigente para una fecha (vigenciaDesde <= fecha <= vigenciaHasta)
    List<PrecioProducto> findByProductoIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(Long productoId, LocalDateTime desde, LocalDateTime hasta);

    List<PrecioProducto> findAllByDeletedAtIsNull();

    // útil: obtener primer precio activo y no eliminado
    Optional<PrecioProducto> findFirstByProductoIdAndActivoTrueAndDeletedAtIsNullOrderByVigenciaDesdeDesc(Long productoId);
}