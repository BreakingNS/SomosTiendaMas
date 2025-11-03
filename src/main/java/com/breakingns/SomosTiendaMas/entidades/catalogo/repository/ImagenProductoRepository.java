package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
    List<ImagenProducto> findByProducto(Producto producto);

    List<ImagenProducto> findByProductoIdOrderByOrdenAsc(Long productoId);
    List<ImagenProducto> findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(Long productoId);

    Optional<ImagenProducto> findFirstByProductoIdOrderByOrdenAsc(Long productoId);

    List<ImagenProducto> findAllByDeletedAtIsNullOrderByProductoIdAscOrdenAsc();

    // operaciones útiles
    void deleteByProductoId(Long productoId);
}
