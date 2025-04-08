package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Usuario;

public interface IUsuarioService {
    
    public Usuario registrar(Usuario usuario);
    public Boolean existeUsuario(String nombreUsuario);
}
