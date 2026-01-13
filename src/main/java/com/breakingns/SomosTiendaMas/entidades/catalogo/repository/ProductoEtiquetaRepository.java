package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoEtiquetaRepository extends JpaRepository<ProductoEtiqueta, Long> {
    List<ProductoEtiqueta> findByProducto(Producto producto);
    List<ProductoEtiqueta> findByEtiqueta(Etiqueta etiqueta);

    // === Nuevos métodos ===
    List<ProductoEtiqueta> findByProductoId(Long productoId);
    List<ProductoEtiqueta> findByEtiquetaId(Long etiquetaId);
    Optional<ProductoEtiqueta> findByProductoIdAndEtiquetaIdAndDeletedAtIsNull(Long productoId, Long etiquetaId);
}