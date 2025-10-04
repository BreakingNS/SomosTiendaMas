package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventarioVarianteRepository extends JpaRepository<InventarioVariante, Long> {
    Optional<InventarioVariante> findByVariante(VarianteProducto variante);
}
