package com.breakingns.SomosTiendaMas.entidades.direccion.model;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "direcciones")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDireccion;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // Puede ser null si pertenece a empresa

    @ManyToOne
    @JoinColumn(name = "id_perfil_empresa")
    private PerfilEmpresa perfilEmpresa; // Puede ser null si pertenece a usuario

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDireccion tipo;

    @Column(length = 200, nullable = false)
    private String calle;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 10)
    private String piso;

    @Column(length = 10)
    private String departamento;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String provincia;

    @Column(length = 10, nullable = false)
    private String codigoPostal;

    @Column(length = 100, nullable = false)
    private String pais;

    @Column(columnDefinition = "TEXT")
    private String referencia;

    @Column(nullable = false)
    private Boolean activa;

    @Column(nullable = false)
    private Boolean esPrincipal;

    @ManyToOne
    @JoinColumn(name = "es_copia_de")
    private Direccion direccionCopiada;

    public enum TipoDireccion {
        PERSONAL, FISCAL, ENVIO, FACTURACION
    }
}