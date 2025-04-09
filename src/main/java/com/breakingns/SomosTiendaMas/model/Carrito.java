package com.breakingns.SomosTiendaMas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Carrito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id_carrito;
    
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Carrito() {
    }

    public Carrito(Long id_carrito, Usuario usuario) {
        this.id_carrito = id_carrito;
        this.usuario = usuario;
    }
    
    
}
