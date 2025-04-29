package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import java.util.Optional;

public interface IUsuarioService {
    
    Optional<Usuario> findById(Long id);
    public Usuario registrar(Usuario usuario);
    public Boolean existeUsuario(String nombreUsuario);
    public Usuario findByUsername(String username);
    public void registrarConRol(Usuario usuario, RolNombre rolNombre);
}
