package com.breakingns.SomosTiendaMas.entidades.direccion.model;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
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

    //----------------------------- Relaciones ----------------------------//

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario; // Puede ser null si pertenece a empresa

    @ManyToOne
    @JoinColumn(name = "id_perfil_empresa", nullable = true)
    private PerfilEmpresa perfilEmpresa; // Puede ser null si pertenece a usuario

    //----------------------------- Tipo de dirección ----------------------------//

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDireccion tipo;

    //----------------------------- Datos de ubicación ----------------------------//

    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @ManyToOne
    @JoinColumn(name = "id_provincia", nullable = false)
    private Provincia provincia;

    @ManyToOne
    @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @ManyToOne
    @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;

    @ManyToOne
    @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

    //----------------------------- Datos de la dirección ----------------------------//

    @Column(length = 200, nullable = false)
    private String calle;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 10)
    private String piso;

    @Column(length = 10)
    private String departamentoInterno;

    @Column(length = 10, nullable = false)
    private String codigoPostal;

    @Column(columnDefinition = "TEXT")
    private String referencia;

    @Column(nullable = false)
    private Boolean activa;

    @Column(nullable = false)
    private Boolean esPrincipal;
    /* 
    @Column(nullable = false)
    private Boolean usarComoEnvio;
    */
    public enum TipoDireccion {
        PERSONAL, FISCAL, ENVIO, FACTURACION
    }
}