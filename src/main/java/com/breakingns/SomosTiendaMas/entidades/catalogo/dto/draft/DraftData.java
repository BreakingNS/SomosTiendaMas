package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioProductoResponseDTO;
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
    private List<ImagenProductoDTO> imagenes;
    private PrecioProductoResponseDTO precio;
    private InventarioProductoDTO inventario;
    private PhysicalPropertiesDTO propiedadesFisicas;
}