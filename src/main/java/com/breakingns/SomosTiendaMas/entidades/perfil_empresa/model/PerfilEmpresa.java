package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionEmpresa;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.PerfilUsuario;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.TelefonoEmpresa;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "perfil_empresa", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cuit"})
})
public class PerfilEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPerfilEmpresa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_perfil_id", unique = true)
    private PerfilUsuario responsable;

    @Column(length = 200, nullable = false)
    private String razonSocial;

    @Column(length = 200, nullable = true)
    private String nombreComercial;

    @Column(length = 15, nullable = false, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionIVA condicionIVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAprobado estadoAprobado;

    @Column(length = 100, nullable = false)
    private String emailEmpresa;

    // Campos importantes
    @Column(columnDefinition = "TEXT", length = 1000)
    private String descripcionEmpresa;

    @Column(length = 255)
    private String sitioWeb;

    @Enumerated(EnumType.STRING)
    private CategoriaEmpresa categoriaEmpresa;

    @Column(nullable = false)
    private Boolean requiereFacturacion = true;

    @Column(precision = 12, scale = 2)
    private BigDecimal limiteCreditoVentas;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    @Column(nullable = false)
    private Boolean activo = true;

    // Campos opcionales
    @Column(length = 500)
    private String logoUrl;

    @Column(length = 7)
    private String colorCorporativo;

    @Column(length = 200)
    private String descripcionCorta;

    @Column(length = 100)
    private String horarioAtencion;

    @Column(length = 50)
    private String diasLaborales;

    private Integer tiempoProcesamientoPedidos;

    // Relaciones bidireccionales
    @JsonIgnore
    @OneToMany(mappedBy = "perfilEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TelefonoEmpresa> telefonos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "perfilEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DireccionEmpresa> direcciones = new ArrayList<>();

    // Enums
    public enum CondicionIVA { RI, MONOTRIBUTO, EXENTO }
    public enum EstadoAprobado { PENDIENTE, APROBADO, RECHAZADO }
    public enum CategoriaEmpresa { RETAIL, MAYORISTA, FABRICANTE }
}
