package com.breakingns.SomosTiendaMas.entidades.telefono.model;

import jakarta.persistence.*;

@Entity
@Table(name = "codigos_area")
public class CodigoArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String codigo; // Ej: "383", "3837"

    @Column(nullable = false)
    private String localidad;

    @Column(nullable = false)
    private String provincia;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
}
