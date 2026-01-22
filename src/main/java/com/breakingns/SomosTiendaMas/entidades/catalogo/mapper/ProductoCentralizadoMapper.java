package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteAnidadaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapper de conveniencia para payloads centralizados (crear producto + variantes).
 * Compone `ProductoMapper` y `VarianteMapper` para producir las entidades listas
 * para persistir. No aplica reglas de negocio/validación compleja.
 */
public final class ProductoCentralizadoMapper {

    private ProductoCentralizadoMapper() {}

    public static Producto toProductoEntity(ProductoCentralizadoCrearDTO dto, Marca marca, Categoria categoria) {
        if (dto == null || dto.getProducto() == null) return null;
        return ProductoMapper.toEntity(dto.getProducto(), marca, categoria);
    }

    public static List<Variante> toVarianteEntities(ProductoCentralizadoCrearDTO dto, Producto producto) {
        if (dto == null || dto.getVariantes() == null) return Collections.emptyList();
        List<Variante> out = new ArrayList<>();
        for (VarianteAnidadaCrearDTO v : dto.getVariantes()) {
            if (v == null) continue;
            // VarianteAnidadaCrearDTO provides a helper to get a flat VarianteCrearDTO
            var flat = v.toFlatVariante();
            Variante ent = VarianteMapper.fromCrearDto(flat, producto);
            out.add(ent);
        }
        return out;
    }

}
