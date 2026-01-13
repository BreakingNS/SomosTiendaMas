package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImagenProductoRepository extends JpaRepository<ImagenVariante, Long> {
    // usar ruta anidada a través de `variante.producto` para evitar depender de un atributo directo `producto`
    List<ImagenVariante> findByVarianteProducto(Producto producto);

    List<ImagenVariante> findByVarianteProductoIdOrderByOrdenAsc(Long productoId);
    List<ImagenVariante> findByVarianteProductoIdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    Optional<ImagenVariante> findFirstByVarianteProductoIdOrderByOrdenAsc(Long productoId);

    List<ImagenVariante> findAllByDeletedAtIsNullOrderByVarianteProductoIdAscOrdenAsc();

    // operaciones útiles
    void deleteByVarianteProductoId(Long productoId);
}