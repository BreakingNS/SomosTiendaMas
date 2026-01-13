package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;

public class ProductoDetalleResponseDTO {

    private ProductoResponseDTO producto;
    private List<ImagenProductoDTO> imagenes;
    private PrecioVarianteResponseDTO precio;
    private DisponibilidadResponseDTO stock;
    private List<OpcionResumenDTO> opciones;

    // Nuevo: propiedades físicas opcionales
    private PhysicalPropertiesDTO physical;

    public ProductoResponseDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoResponseDTO producto) {
        this.producto = producto;
    }

    public List<ImagenProductoDTO> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenProductoDTO> imagenes) {
        this.imagenes = imagenes;
    }

    public PrecioVarianteResponseDTO getPrecio() {
        return precio;
    }

    public void setPrecio(PrecioVarianteResponseDTO precio) {
        this.precio = precio;
    }

    public DisponibilidadResponseDTO getStock() {
        return stock;
    }

    public void setStock(DisponibilidadResponseDTO stock) {
        this.stock = stock;
    }

    public List<OpcionResumenDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionResumenDTO> opciones) {
        this.opciones = opciones;
    }

    public PhysicalPropertiesDTO getPhysical() {
        return physical;
    }

    public void setPhysical(PhysicalPropertiesDTO physical) {
        this.physical = physical;
    }
}