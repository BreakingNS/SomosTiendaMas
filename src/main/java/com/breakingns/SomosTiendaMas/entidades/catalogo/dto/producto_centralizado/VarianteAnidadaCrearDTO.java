package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import jakarta.validation.Valid;

/**
 * Variante con sub-recursos embebidos para payloads centralizados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VarianteAnidadaCrearDTO {
    // campos base de variante
    private String sku;
    private String attributesJson;
    private String attributesHash;
    private Boolean esDefault;
    private Boolean activo;

    // sub-recursos por variante
    @Valid
    private List<VarianteOpcionesAsignarDTO> varianteOpciones;

    @Valid
    private List<PrecioVarianteCrearDTO> precios;

    @Valid
    private List<InventarioVarianteDTO> inventarios;

    @Valid
    private List<PhysicalPropertiesDTO> physical;

    @Valid
    private List<ImagenVarianteDTO> imagenes;

    // helper: permitir reutilizar VarianteCrearDTO si se necesita
    public VarianteCrearDTO toFlatVariante() {
        VarianteCrearDTO v = new VarianteCrearDTO();
        // VarianteCrearDTO fields: sku, attributesJson, attributesHash, esDefault, activo
        v.setSku(this.sku);
        v.setAttributesJson(this.attributesJson);
        v.setAttributesHash(this.attributesHash);
        v.setEsDefault(this.esDefault);
        v.setActivo(this.activo);
        return v;
    }
}
