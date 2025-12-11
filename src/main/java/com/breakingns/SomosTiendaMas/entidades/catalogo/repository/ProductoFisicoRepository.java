package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoFisico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoFisicoRepository extends JpaRepository<ProductoFisico, Long> {
    Optional<ProductoFisico> findByProducto_Id(Long productoId);
    Optional<ProductoFisico> findByProducto_IdAndDeletedAtIsNull(Long productoId);
    void deleteByProducto_Id(Long productoId);
}