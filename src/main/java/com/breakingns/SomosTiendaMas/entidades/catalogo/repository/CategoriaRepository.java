package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findBySlug(String slug);

    // === Nuevos métodos ===
    Optional<Categoria> findBySlugAndDeletedAtIsNull(String slug);
    List<Categoria> findAllByDeletedAtIsNullOrderByNombreAsc();

    // Trae la categoría y su padre en la misma consulta (evita lazy init / n+1)
    @Query("select c from Categoria c left join fetch c.categoriaPadre where c.id = :id")
    Optional<Categoria> findByIdWithParent(@Param("id") Long id);
}