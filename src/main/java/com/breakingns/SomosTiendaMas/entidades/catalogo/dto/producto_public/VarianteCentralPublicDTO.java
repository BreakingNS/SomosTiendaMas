package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public;

import lombok.Data;
import java.util.List;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;

@Data
public class VarianteCentralPublicDTO {
    private Long id;
    private Long productoId;
    private String skuResuelto;
    private Boolean esDefault;
    private Boolean activo;
    private PrecioPublicDTO precio;
    private DisponibilidadPublicDTO disponible;
    private List<OpcionPublicDTO> opciones;
    private List<ImagenPublicDTO> imagenes;
    private PhysicalPropertiesDTO physical;
}
