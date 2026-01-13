package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {
    private ProductoCrearDTO producto;
    private ProductoOpcionesAsignarDTO opciones;        // asignar opciones/valores
    private PhysicalPropertiesDTO physical;
}