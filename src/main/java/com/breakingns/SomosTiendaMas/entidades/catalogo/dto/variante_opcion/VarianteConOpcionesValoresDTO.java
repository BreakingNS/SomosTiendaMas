package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion;

import java.util.List;

public class VarianteConOpcionesValoresDTO {
    private Long varianteId;
    private List<OpcionConValoresDTO> opciones;

    public VarianteConOpcionesValoresDTO() {}

    public VarianteConOpcionesValoresDTO(Long varianteId, List<OpcionConValoresDTO> opciones) {
        this.varianteId = varianteId;
        this.opciones = opciones;
    }

    public Long getVarianteId() { return varianteId; }
    public void setVarianteId(Long varianteId) { this.varianteId = varianteId; }
    public List<OpcionConValoresDTO> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionConValoresDTO> opciones) { this.opciones = opciones; }
}
