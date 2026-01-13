package com.breakingns.SomosTiendaMas.entidades.catalogo.repository;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PlantillaCampo;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoAtributo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoAtributoRepository extends JpaRepository<ProductoAtributo, Long> {
    List<ProductoAtributo> findByProducto(Producto producto);
    List<ProductoAtributo> findByProductoId(Long productoId);
    List<ProductoAtributo> findByPlantillaCampo(PlantillaCampo plantillaCampo);
    Optional<ProductoAtributo> findByProductoIdAndSlugIgnoreCase(Long productoId, String slug);
    List<ProductoAtributo> findByProductoIdOrderByIdAsc(Long productoId);
    void deleteByProductoId(Long productoId);
}