package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto; // agregado

import java.util.List;

public class ProductoMapper {

    public static Producto toEntity(ProductoCrearDTO dto, Marca marca, Categoria categoria) {
        Producto p = new Producto();
        p.setNombre(dto.getNombre());
        p.setSlug(dto.getSlug());
        p.setDescripcion(dto.getDescripcion());
        // mapear garantía y política de devoluciones al crear
        p.setGarantia(dto.getGarantia());
        p.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());
        p.setMarca(marca);
        p.setCategoria(categoria);
        p.setMetadataJson(dto.getMetadataJson());
        // mapear condicion (si viene) o usar default NUEVO
        p.setCondicion(dto.getCondicion() != null ? dto.getCondicion() : CondicionProducto.NUEVO);
        return p;
    }

    public static void applyActualizar(ProductoActualizarDTO dto, Producto p, Marca marca, Categoria categoria) {
        if (dto == null || p == null) return;
        if (dto.getNombre() != null) p.setNombre(dto.getNombre());
        if (dto.getSlug() != null) p.setSlug(dto.getSlug());
        if (dto.getDescripcion() != null) p.setDescripcion(dto.getDescripcion());
        // actualizar garantía y política si vienen
        if (dto.getGarantia() != null) p.setGarantia(dto.getGarantia());
        if (dto.getPoliticaDevoluciones() != null) p.setPoliticaDevoluciones(dto.getPoliticaDevoluciones());

        if (dto.getMarcaId() != null && marca != null) p.setMarca(marca);
        if (dto.getCategoriaId() != null && categoria != null) p.setCategoria(categoria);
        if (dto.getMetadataJson() != null) p.setMetadataJson(dto.getMetadataJson());
        // actualizar condicion si se provee
        if (dto.getCondicion() != null) p.setCondicion(dto.getCondicion());
    }

    public static ProductoResponseDTO toResponse(Producto p) {
        if (p == null) return null;

        // intentamos extraer info de categoría / padre si ya está cargada
        Long idCategoriaPadre = null;
        Long idCategoriaHija = null;
        String nombreCategoriaPadre = null;
        String nombreCategoriaHija = null;
        if (p.getCategoria() != null) {
            idCategoriaHija = p.getCategoria().getId();
            nombreCategoriaHija = p.getCategoria().getNombre();
            if (p.getCategoria().getCategoriaPadre() != null) {
                idCategoriaPadre = p.getCategoria().getCategoriaPadre().getId();
                nombreCategoriaPadre = p.getCategoria().getCategoriaPadre().getNombre();
            }
        }

        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .slug(p.getSlug())
                .descripcion(p.getDescripcion())
                .garantia(p.getGarantia())
                .politicaDevoluciones(p.getPoliticaDevoluciones())
                .marcaId(p.getMarca() != null ? p.getMarca().getId() : null)
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .idCategoriaPadre(idCategoriaPadre)
                .idCategoriaHija(idCategoriaHija)
                .nombreCategoriaPadre(nombreCategoriaPadre)
                .nombreCategoriaHija(nombreCategoriaHija)
                .estado(p.getEstadoProducto())
                .visibilidad(p.getVisibilidad())
                .condicion(p.getCondicion())
                .metadataJson(p.getMetadataJson())
                .atributosJson(p.getAtributosJson())
                .sku(p.getSku())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .deletedAt(p.getDeletedAt())
                .build();
    }

    public static ProductoListaDTO toLista(Producto p, Long precioCentavos, Long disponible) {
        if (p == null) return null;
        ProductoListaDTO dto = new ProductoListaDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setSlug(p.getSlug());
        dto.setMarcaId(p.getMarca() != null ? p.getMarca().getId() : null);
        dto.setCategoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null);
        dto.setSku(p.getSku());
        dto.setPrecioCentavos(precioCentavos);
        dto.setDisponible(disponible != null ? disponible : 0L);
        dto.setCondicion(p.getCondicion()); // agregado
        return dto;
    }

    public static ProductoDetalleDTO toDetalle(
            Producto p,
            List<Long> imagenIds,
            List<String> imagenUrls,
            Long precioActualCentavos,
            Boolean precioActivo,
            long disponible
    ) {
        if (p == null) return null;
        ProductoDetalleDTO dto = new ProductoDetalleDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setSlug(p.getSlug());
        dto.setDescripcion(p.getDescripcion());
        dto.setMarcaId(p.getMarca() != null ? p.getMarca().getId() : null);
        dto.setCategoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null);
        dto.setSku(p.getSku());
        dto.setAtributosJson(p.getAtributosJson());
        dto.setMetadataJson(p.getMetadataJson());

        dto.setImagenIds(imagenIds);
        dto.setImagenUrls(imagenUrls);
        dto.setPrecioActualCentavos(precioActualCentavos);
        dto.setPrecioActivo(precioActivo);
        dto.setDisponible(disponible);
        dto.setGarantia(p.getGarantia());
        dto.setPoliticaDevoluciones(p.getPoliticaDevoluciones());

        dto.setCondicion(p.getCondicion()); // agregado

        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        dto.setDeletedAt(p.getDeletedAt());
        return dto;
    }
}