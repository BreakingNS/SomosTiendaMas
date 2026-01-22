package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public;

import lombok.Data;
import java.util.List;

@Data
public class ProductoCentralizadoPublicDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private String marcaNombre;
    private String nombreCategoriaPadre;
    private String nombreCategoriaHija;
    private String sku;
    private String condicion;
    private String garantia;
    private String politicaDevoluciones;
    private String skuResuelto;
    private List<VarianteCentralPublicDTO> variantes;
}
