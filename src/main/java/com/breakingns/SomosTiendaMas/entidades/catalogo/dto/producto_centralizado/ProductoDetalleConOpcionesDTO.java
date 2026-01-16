package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.OpcionConValoresDTO;

public class ProductoDetalleConOpcionesDTO {

    private ProductoResponseDTO producto;
    private List<OpcionConValoresDTO> opciones;
    private List<ImagenVarianteDTO> imagenes;
    private PrecioVarianteResponseDTO precio;
    private DisponibilidadResponseDTO stock;
    // REVIEW: se deja de utilizar fisicar para producto, solo para variantes.
    // @Deprecated(since="2026-01-15", forRemoval=true)
    // private PhysicalPropertiesDTO physical;

    public ProductoResponseDTO getProducto() { return producto; }
    public void setProducto(ProductoResponseDTO producto) { this.producto = producto; }

    public List<OpcionConValoresDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionConValoresDTO> opciones) { this.opciones = opciones; }
    /* 
    public List<ImagenVarianteDTO> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenVarianteDTO> imagenes) { this.imagenes = imagenes; }

    public PrecioVarianteResponseDTO getPrecio() { return precio; }
    public void setPrecio(PrecioVarianteResponseDTO precio) { this.precio = precio; }

    public DisponibilidadResponseDTO getStock() { return stock; }
    public void setStock(DisponibilidadResponseDTO stock) { this.stock = stock; }
    
    public PhysicalPropertiesDTO getPhysical() { return physical; }
    public void setPhysical(PhysicalPropertiesDTO physical) { this.physical = physical; }
    */
}
