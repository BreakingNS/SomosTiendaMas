package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySlug(String slug);

    // === Nuevos métodos ===
    Optional<Producto> findBySlugAndDeletedAtIsNull(String slug);
    List<Producto> findByCategoria_IdAndDeletedAtIsNull(Long categoriaId);
    List<Producto> findByMarca_IdAndDeletedAtIsNull(Long marcaId);
    List<Producto> findAllByDeletedAtIsNull();

    // Opcional: consultas por condicion
    List<Producto> findByCondicionAndDeletedAtIsNull(CondicionProducto condicion);
}