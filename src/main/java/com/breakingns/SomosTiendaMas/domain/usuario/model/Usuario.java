package com.breakingns.SomosTiendaMas.domain.usuario.model;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.model.Carrito;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long idUsuario;

    private String username;
    private String password;
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SesionActiva> sesionesActivas = new ArrayList<>();
    
    @OneToOne(mappedBy = "usuario")
    private Carrito carrito;
    
    public Usuario() {}

    public Usuario(Long idUsuario, String username, String password, String email, Carrito carrito) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.password = password;
        this.email = email;
        this.carrito = carrito;
    }
    
}
