package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrecioVarianteRepository extends JpaRepository<PrecioVariante, Long> {
    List<PrecioVariante> findByVarianteAndActivoTrue(VarianteProducto variante);

    @Query("select p from PrecioVariante p " +
           "where p.variante = :variante and p.activo = true " +
           "and (p.vigenciaDesde is null or p.vigenciaDesde <= :ahora) " +
           "and (p.vigenciaHasta is null or p.vigenciaHasta >= :ahora)")
    Optional<PrecioVariante> findPrecioVigente(VarianteProducto variante, LocalDateTime ahora);
}
