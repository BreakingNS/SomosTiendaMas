package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrecioVarianteRepository extends JpaRepository<PrecioVariante, Long> {
    List<PrecioVariante> findByVarianteProductoIdOrderByVigenciaDesdeDesc(Long productoId);
    List<PrecioVariante> findByVarianteProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(Long productoId);
    Optional<PrecioVariante> findFirstByVarianteProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(Long productoId);

    // precio vigente para una fecha (vigenciaDesde <= fecha <= vigenciaHasta)
    List<PrecioVariante> findByVarianteProductoIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(Long productoId, LocalDateTime desde, LocalDateTime hasta);

    List<PrecioVariante> findAllByDeletedAtIsNull();

    // útil: obtener primer precio activo y no eliminado
    Optional<PrecioVariante> findFirstByVarianteProductoIdAndActivoTrueAndDeletedAtIsNullOrderByVigenciaDesdeDesc(Long productoId);

    // Variante-scoped queries (por varianteId)
    List<PrecioVariante> findByVarianteIdOrderByVigenciaDesdeDesc(Long varianteId);
    List<PrecioVariante> findByVarianteIdAndActivoTrueOrderByVigenciaDesdeDesc(Long varianteId);
    Optional<PrecioVariante> findFirstByVarianteIdAndActivoTrueOrderByVigenciaDesdeDesc(Long varianteId);
    Optional<PrecioVariante> findFirstByVarianteIdAndActivoTrueAndDeletedAtIsNullOrderByVigenciaDesdeDesc(Long varianteId);

    List<PrecioVariante> findByVarianteIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(Long varianteId, LocalDateTime desde, LocalDateTime hasta);
}