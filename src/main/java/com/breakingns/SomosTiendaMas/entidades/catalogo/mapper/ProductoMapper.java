package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;

public class ProductoMapper {

    public static Producto toEntity(ProductoCrearDTO dto, Marca marca, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setSlug(dto.getSlug());
        p.setDescripcion(dto.getDescripcion());
        p.setMarca(marca);
        p.setCategoria(categoria);
        p.setMetadataJson(dto.getMetadataJson());
        return p;
    }

    public static ProductoResponseDTO toResponse(Producto p) {
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .slug(p.getSlug())
                .descripcion(p.getDescripcion())
                .marcaId(p.getMarca() != null ? p.getMarca().getId() : null)
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .estado(p.getEstado())
                .visibilidad(p.getVisibilidad())
                .metadataJson(p.getMetadataJson())
                .build();
    }
}
