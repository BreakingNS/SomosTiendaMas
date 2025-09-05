package com.breakingns.SomosTiendaMas.entidades.telefono.model;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "telefonos")
public class Telefono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTelefono;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // Puede ser null si pertenece a empresa

    @ManyToOne
    @JoinColumn(name = "id_perfil_empresa")
    private PerfilEmpresa perfilEmpresa; // Puede ser null si pertenece a usuario

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTelefono tipo;

    @Column(length = 20, nullable = false)
    private String numero;

    @Column(length = 10, nullable = false)
    private String caracteristica;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private Boolean verificado;

    @ManyToOne
    @JoinColumn(name = "es_copia_de")
    private Telefono telefonoCopiado;

    public enum TipoTelefono {
        PRINCIPAL, SECUNDARIO, EMPRESA, WHATSAPP
    }
}