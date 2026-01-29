package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import java.util.List;

/**
 * Variante response sin el bloque `varianteOpciones` (usa `attributesJson` para mostrar opciones en UI).
 */
public class VarianteCentralizadaResponseSimpleDTO {
    private Long id;
    private String sku;
    private String attributesJson;
    private String attributesHash;
    private Boolean esDefault;
    private Boolean activo;

    private List<PrecioVarianteResponseDTO> precios;
    private List<InventarioVarianteDTO> inventarios;
    private List<PhysicalPropertiesDTO> physical;
    private List<ImagenVarianteDTO> imagenes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
    public String getAttributesHash() { return attributesHash; }
    public void setAttributesHash(String attributesHash) { this.attributesHash = attributesHash; }
    public Boolean getEsDefault() { return esDefault; }
    public void setEsDefault(Boolean esDefault) { this.esDefault = esDefault; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public List<PrecioVarianteResponseDTO> getPrecios() { return precios; }
    public void setPrecios(List<PrecioVarianteResponseDTO> precios) { this.precios = precios; }
    public List<InventarioVarianteDTO> getInventarios() { return inventarios; }
    public void setInventarios(List<InventarioVarianteDTO> inventarios) { this.inventarios = inventarios; }
    public List<PhysicalPropertiesDTO> getPhysical() { return physical; }
    public void setPhysical(List<PhysicalPropertiesDTO> physical) { this.physical = physical; }
    public List<ImagenVarianteDTO> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenVarianteDTO> imagenes) { this.imagenes = imagenes; }
}
