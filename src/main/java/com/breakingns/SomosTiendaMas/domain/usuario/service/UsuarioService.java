package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.security.exception.RolNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioYaExisteException;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
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
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public void registrarConRol(Usuario usuario, RolNombre rolNombre) {
        if (existeUsuario(usuario.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }

        Rol rol = rolService.getByNombre(rolNombre)
                .orElseThrow(() -> new RolNoEncontradoException("Error: Rol no encontrado."));

        usuario.getRoles().add(rol);
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
    
    public void changePassword(Usuario usuario, String currentPassword, String newPassword) {
        System.out.println("1.1. entrando a changePassword");
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        System.out.println("1.2. se realiza el cambio");
        usuario.setPassword(passwordEncoder.encode(newPassword));
        System.out.println("1.3. se seteo el password sin encriptar");
        usuarioRepository.save(usuario);
    }
    
    
    
}
