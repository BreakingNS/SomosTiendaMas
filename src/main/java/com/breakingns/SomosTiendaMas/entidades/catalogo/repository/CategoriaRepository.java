package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findBySlug(String slug);

    // === Nuevos métodos ===
    Optional<Categoria> findBySlugAndDeletedAtIsNull(String slug);
    List<Categoria> findAllByDeletedAtIsNullOrderByNombreAsc();
}
