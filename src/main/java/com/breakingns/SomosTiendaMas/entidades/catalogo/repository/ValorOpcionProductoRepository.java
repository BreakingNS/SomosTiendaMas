package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValorOpcionProductoRepository extends JpaRepository<ValorOpcionProducto, Long> {
    List<ValorOpcionProducto> findByOpcionOrderByOrdenAsc(OpcionProducto opcion);
}
