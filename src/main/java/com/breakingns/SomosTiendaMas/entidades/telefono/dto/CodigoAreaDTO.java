package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

public class CodigoAreaDTO {

    private String codigo;
    private String localidad;
    private String provincia;

    public CodigoAreaDTO() {}

    public CodigoAreaDTO(String codigo, String localidad, String provincia) {
        this.codigo = codigo;
        this.localidad = localidad;
        this.provincia = provincia;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
}