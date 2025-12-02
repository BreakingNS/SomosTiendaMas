package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import java.util.List;

public class OpcionConValoresDTO {
    private Long opcionId;
    private String nombre;
    private Integer orden;
    private List<OpcionValorResponseDTO> valores;

    public OpcionConValoresDTO() {}

    public OpcionConValoresDTO(Long opcionId, String nombre, Integer orden, List<OpcionValorResponseDTO> valores) {
        this.opcionId = opcionId;
        this.nombre = nombre;
        this.orden = orden;
        this.valores = valores;
    }

    public Long getOpcionId() { return opcionId; }
    public void setOpcionId(Long opcionId) { this.opcionId = opcionId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public List<OpcionValorResponseDTO> getValores() { return valores; }
    public void setValores(List<OpcionValorResponseDTO> valores) { this.valores = valores; }
}