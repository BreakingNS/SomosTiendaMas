package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcionProductoRepository extends JpaRepository<OpcionProducto, Long> {
    List<OpcionProducto> findByProductoOrderByOrdenAsc(Producto producto);
}
