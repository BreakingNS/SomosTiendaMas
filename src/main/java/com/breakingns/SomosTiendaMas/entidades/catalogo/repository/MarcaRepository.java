package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    Optional<Marca> findBySlug(String slug);

    // === Nuevos métodos ===
    Optional<Marca> findBySlugAndDeletedAtIsNull(String slug);
    // Buscar marcas por categoría, solo activas (deletedAt IS NULL)
    List<Marca> findDistinctByCategorias_IdAndDeletedAtIsNullOrderByNombreAsc(Long categoriaId);
}
