package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Carrito;
import java.util.Optional;

public interface ICarritoService {
    
    String crearCarrito(Long id_usuario);
    Optional<Carrito> traerCarritoPorIdUsuario(Long id_usuario);
    
}
