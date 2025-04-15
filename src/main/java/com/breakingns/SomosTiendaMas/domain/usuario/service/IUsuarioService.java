package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import java.util.Optional;

public interface IUsuarioService {
    
    Optional<Usuario> findById(Long id);
    public Usuario registrar(Usuario usuario);
    public Boolean existeUsuario(String nombreUsuario);
    public Usuario findByUsername(String username);
    
}
