package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySlug(String slug);

    // === Nuevos métodos ===
    Optional<Producto> findBySlugAndDeletedAtIsNull(String slug);
    List<Producto> findByCategoria_IdAndDeletedAtIsNull(Long categoriaId);
    List<Producto> findByMarca_IdAndDeletedAtIsNull(Long marcaId);
    List<Producto> findAllByDeletedAtIsNull();
}