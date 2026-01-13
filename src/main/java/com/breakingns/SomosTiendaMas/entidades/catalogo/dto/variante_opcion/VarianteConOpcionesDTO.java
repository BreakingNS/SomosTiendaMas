package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResumenDTO;

public class VarianteConOpcionesDTO {
    private Long varianteId;
    private List<OpcionResumenDTO> opciones;

    public VarianteConOpcionesDTO() {}

    public VarianteConOpcionesDTO(Long varianteId, List<OpcionResumenDTO> opciones) {
        this.varianteId = varianteId;
        this.opciones = opciones;
    }

    public Long getVarianteId() { return varianteId; }
    public void setVarianteId(Long varianteId) { this.varianteId = varianteId; }

    public List<OpcionResumenDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionResumenDTO> opciones) { this.opciones = opciones; }
}