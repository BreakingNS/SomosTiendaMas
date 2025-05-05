package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import java.util.Optional;

public interface IUsuarioService {
    
    Optional<Usuario> findById(Long id);
    
    Usuario registrar(Usuario usuario);
    
    Boolean existeUsuario(String nombreUsuario);
    
    Usuario findByUsername(String username);
    
    void registrarConRol(Usuario usuario, RolNombre rolNombre);
    
    void registrarSinRol(Usuario usuario); // SOLO PRUEBA, no produccion
    
}
