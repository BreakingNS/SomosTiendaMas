package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    private ProductoCrearDTO producto;
    private ProductoOpcionesAsignarDTO opciones;        // asignar opciones/valores
    private PrecioProductoCrearDTO precio;
    private InventarioProductoDTO inventario;          // o crear InventarioProductoCrearDTO si lo prefieres
    private List<ImagenProductoDTO> imagenes;
    private PhysicalPropertiesDTO physical;
}
