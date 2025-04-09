package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Usuario;
import java.util.Optional;

public interface IUsuarioService {
    
    Optional<Usuario> findById(Long id);
    public Usuario registrar(Usuario usuario);
    public Boolean existeUsuario(String nombreUsuario);
    public Usuario findByUsername(String username);
    
}
