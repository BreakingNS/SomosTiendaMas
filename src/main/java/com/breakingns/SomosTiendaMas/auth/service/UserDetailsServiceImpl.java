package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
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

        // Verificar si el usuario tiene roles asignados
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            // Si el usuario no tiene roles, asignamos un rol predeterminado
            log.warn("El usuario {} no tiene roles asignados, asignando ROLE_NONE", username);

            // Asignamos un rol predeterminado
            return new UserAuthDetails(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_NONE")), // Aquí asignamos un rol predeterminado
                usuario
            );
        }

        // Si tiene roles, devolvemos el usuario con los roles asignados
        return new UserAuthDetails(
            usuario.getIdUsuario(),
            usuario.getUsername(),
            usuario.getPassword(),
            mapearRoles(usuario.getRoles()), // Mapear los roles asignados
            usuario
        );
    }
    
    /*
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Cargando usuario desde username: {}", username);

        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            throw new UsernameNotFoundException("El usuario no tiene roles asignados");
        }

        return new UserAuthDetails(
            usuario.getIdUsuario(),
            usuario.getUsername(),
            usuario.getPassword(),
            mapearRoles(usuario.getRoles()),
            usuario
        );
    }
    */

    private Collection<? extends GrantedAuthority> mapearRoles(Set<Rol> roles) {
        log.info("Se mapean los roles: {}", roles);
        return roles.stream()
            .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
            .collect(Collectors.toList());
    }
}