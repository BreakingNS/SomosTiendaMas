package com.breakingns.SomosTiendaMas.model;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

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
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public Carrito() {
    }

    public Carrito(Long id_carrito, Usuario usuario) {
        this.id_carrito = id_carrito;
        this.usuario = usuario;
    }

        @Override
        public String toString() {
            return "Carrito{" +
                    "id_carrito=" + id_carrito +
                    ", usuario_id=" + (usuario != null ? usuario.getIdUsuario() : null) +
                    '}';
        }

    
    
}
