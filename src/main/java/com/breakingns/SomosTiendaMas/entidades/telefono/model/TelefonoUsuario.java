// TODO: deprecado, unificado en Telefono.java con un campo discriminador "entidad" (CLIENTE, EMPRESA, PROVEEDOR) para evitar duplicación de código y facilitar mantenimiento. Mantener esta clase solo si se necesita una tabla separada por razones de rendimiento o seguridad, pero preferir la unificación si no hay una necesidad clara.
/*package com.breakingns.SomosTiendaMas.entidades.telefono.model;

import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.Perfil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "usuario_telefono", indexes = {
        @Index(name = "ix_tel_usuario_perfil", columnList = "perfil_usuario_id")
})
public class TelefonoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_usuario_id", nullable = false)
    private Perfil perfilUsuario;

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

    @Column(nullable = false)
    private Boolean favorito = false;

    public enum TipoTelefono { PRINCIPAL, SECUNDARIO, WHATSAPP }
}
*/