package com.breakingns.SomosTiendaMas.service;

import com.breakingns.SomosTiendaMas.model.Usuario;
import com.breakingns.SomosTiendaMas.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements IUsuarioService{

    @Autowired
    private IUsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Boolean existeUsuario(String nombreUsuario) {
        return usuarioRepository.existsByUsername(nombreUsuario);
    }
    
}
