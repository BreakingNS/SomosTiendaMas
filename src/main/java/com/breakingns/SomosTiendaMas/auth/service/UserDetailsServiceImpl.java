package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Autowired
    public UserDetailsServiceImpl(final IUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Cargando usuario desde username: {}", username);

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verificar si el usuario tiene rol asignado
        if (usuario.getRol() == null) {
            log.warn("El usuario {} no tiene rol asignado", username);
            throw new BadCredentialsException("El usuario no tiene rol asignado");
        }

        // Devolver el usuario con el rol asignado
        return new UserAuthDetails(
            usuario.getIdUsuario(),
            usuario.getUsername(),
            usuario.getPassword(),
            mapearRol(usuario.getRol()), // Mapear el rol asignado
            usuario
        );
    }

    private Collection<? extends GrantedAuthority> mapearRol(Rol rol) {
        log.info("Se mapea el rol: {}", rol);
        return List.of(new SimpleGrantedAuthority(rol.getNombre().name()));
    }

    private Collection<? extends GrantedAuthority> mapearRoles(Set<Rol> roles) {
        log.info("Se mapean los roles: {}", roles);
        return roles.stream()
            .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
            .collect(Collectors.toList());
    }
}