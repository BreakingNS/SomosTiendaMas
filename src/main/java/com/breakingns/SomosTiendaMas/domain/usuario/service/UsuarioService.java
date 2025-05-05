package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.security.exception.PasswordIncorrectaException;
import com.breakingns.SomosTiendaMas.security.exception.RolNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioYaExisteException;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements IUsuarioService{
    
    private final CarritoService carritoService;
    private final RolService rolService;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(CarritoService carritoService, RolService rolService, IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.carritoService = carritoService;
        this.rolService = rolService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void registrarConRol(Usuario usuario, RolNombre rolNombre) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }

        if (existeUsuario(usuario.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UsuarioYaExisteException("El correo electrónico ya está en uso");
        }

        Rol rol = rolService.getByNombre(rolNombre)
                .orElseThrow(() -> new RolNoEncontradoException("Error: Rol no encontrado."));

        usuario.getRoles().add(rol);
        registrar(usuario);
        carritoService.crearCarrito(usuario.getIdUsuario());
    }
    
    @Override
    @Transactional // SOLO PRUEBA, no producciion
    public void registrarSinRol(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }

        if (existeUsuario(usuario.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UsuarioYaExisteException("El correo electrónico ya está en uso");
        }

        usuario.getRoles().add(null);
        registrar(usuario);
        carritoService.crearCarrito(usuario.getIdUsuario());
    }

    @Override
    public Boolean existeUsuario(String nombreUsuario) {
        return usuarioRepository.existsByUsername(nombreUsuario);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }
    
    @Override
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
    }

}
