package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VarianteOpcionValorRepository extends JpaRepository<VarianteOpcionValor, Long> {
    List<VarianteOpcionValor> findByVariante(VarianteProducto variante);

    void deleteByVariante(VarianteProducto variante);
}
