package com.breakingns.SomosTiendaMas.entidades.telefono.model;

import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "telefono_empresa", indexes = {
        @Index(name = "ix_tel_empresa_perfil", columnList = "perfil_empresa_id")
})
public class TelefonoEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_empresa_id", nullable = false)
    private PerfilEmpresa perfilEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTelefono tipo;

    @Column(length = 20, nullable = false)
    private String numero;

    @Column(length = 10, nullable = false)
    private String caracteristica;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean verificado = false;

    public enum TipoTelefono { EMPRESA, PRINCIPAL, SECUNDARIO }
}
