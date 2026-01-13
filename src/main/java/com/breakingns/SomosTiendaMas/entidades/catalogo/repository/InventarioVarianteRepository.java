package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioVarianteRepository extends JpaRepository<InventarioVariante, Long> {
    Optional<InventarioVariante> findByVariante(Variante variante);

    Optional<InventarioVariante> findByVarianteId(Long varianteId);
    Optional<InventarioVariante> findByVarianteIdAndDeletedAtIsNull(Long varianteId);

    List<InventarioVariante> findAllByDeletedAtIsNull();
    List<InventarioVariante> findByOnHandLessThan(Long threshold);
    List<InventarioVariante> findByReservedGreaterThan(Long threshold);

    // operaciones convenientes
    void deleteByVarianteId(Long varianteId);
}