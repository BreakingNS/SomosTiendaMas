package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftData {
    private ProductoCrearDTO producto;
    private OpcionCrearDTO opcion;
    private List<ImagenVarianteDTO> imagenes;
    private PrecioVarianteResponseDTO precio;
    private InventarioVarianteDTO inventario;
    private PhysicalPropertiesDTO propiedadesFisicas;
}