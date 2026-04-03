//TODO: deprecado, unificado en Direcicon.java con un campo discriminador "entidad" (CLIENTE, EMPRESA, PROVEEDOR) para evitar duplicación de código y facilitar mantenimiento. Mantener esta clase solo si se necesita una tabla separada por razones de rendimiento o seguridad, pero preferir la unificación si no hay una necesidad clara.
/* 
package com.breakingns.SomosTiendaMas.entidades.direccion.model;

import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.auth.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "empresa_direccion", indexes = {
        @Index(name = "ix_dir_empresa_perfil", columnList = "perfil_empresa_id")
})
public class DireccionEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_empresa_id", nullable = false)
    private PerfilEmpresa perfilEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDireccion tipo;

    @ManyToOne @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;

    @ManyToOne @JoinColumn(name = "id_provincia", nullable = false)
    private Provincia provincia;

    @ManyToOne @JoinColumn(name = "id_departamento", nullable = false)
    private Departamento departamento;

    @ManyToOne @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;

    @ManyToOne @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

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
    private Boolean activa = true;

    @Column(nullable = false)
    private Boolean esPrincipal = true;

    public enum TipoDireccion { FISCAL, ENVIO, FACTURACION }
}*/