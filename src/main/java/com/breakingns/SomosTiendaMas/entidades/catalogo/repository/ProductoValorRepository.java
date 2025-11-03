package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoValorRepository extends JpaRepository<ProductoValor, Long> {
    List<ProductoValor> findByProducto(Producto producto);
    List<ProductoValor> findByValor(OpcionValor valor);

    List<ProductoValor> findByProductoId(Long productoId);
    List<ProductoValor> findByValorId(Long valorId);

    Optional<ProductoValor> findByProductoIdAndValorId(Long productoId, Long valorId);
    List<ProductoValor> findAllByDeletedAtIsNull();

    void deleteByProductoId(Long productoId);
    void deleteByValorId(Long valorId);
}