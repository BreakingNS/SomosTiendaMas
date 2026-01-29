package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioVarianteRepository extends JpaRepository<InventarioVariante, Long> {
    Optional<InventarioVariante> findByVariante(Variante variante);

    Optional<InventarioVariante> findByVarianteId(Long varianteId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioVariante i WHERE i.variante.id = :varianteId")
    Optional<InventarioVariante> findByVarianteIdForUpdate(@Param("varianteId") Long varianteId);
    Optional<InventarioVariante> findByVarianteIdAndDeletedAtIsNull(Long varianteId);

    List<InventarioVariante> findAllByDeletedAtIsNull();
    List<InventarioVariante> findByOnHandLessThan(Long threshold);
    List<InventarioVariante> findByReservedGreaterThan(Long threshold);

    // operaciones convenientes
    void deleteByVarianteId(Long varianteId);
}