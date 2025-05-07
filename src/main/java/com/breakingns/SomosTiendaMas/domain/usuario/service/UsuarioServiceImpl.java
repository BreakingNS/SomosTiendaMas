package com.breakingns.SomosTiendaMas.domain.usuario.service;

import com.breakingns.SomosTiendaMas.auth.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.security.exception.ContrasenaVaciaException;
import com.breakingns.SomosTiendaMas.security.exception.EmailInvalidoException;
import com.breakingns.SomosTiendaMas.security.exception.EmailYaRegistradoException;
import com.breakingns.SomosTiendaMas.security.exception.NombreUsuarioVacioException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordIncorrectaException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordInvalidaException;
import com.breakingns.SomosTiendaMas.security.exception.RolNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioYaExisteException;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements IUsuarioService{
    
    private final CarritoService carritoService;
    private final RolService rolService;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(CarritoService carritoService, RolService rolService, IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
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
    public void registrarConRolDesdeDTO(RegistroUsuarioDTO dto, RolNombre rolNombre) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede estar vacío");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ContrasenaVaciaException("La contraseña no puede estar vacía");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new EmailInvalidoException("El correo electrónico no tiene un formato válido");
        }

        if (existeUsuario(dto.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailYaRegistradoException("El correo electrónico ya está en uso");
        }

        if (dto.getPassword().length() < 6) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres.");
        }

        if (dto.getPassword().length() > 16) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.");
        }

        if (dto.getUsername().equals("forzar-error")) {
            throw new RuntimeException("Error interno en el servidor");
        }

        Rol rol = rolService.getByNombre(rolNombre)
                .orElseThrow(() -> new RolNoEncontradoException("Error: Rol no encontrado."));

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword()); // Asegurate que después se encripta
        usuario.setEmail(dto.getEmail());
        usuario.getRoles().add(rol);

        registrar(usuario);
        carritoService.crearCarrito(usuario.getIdUsuario());
    }
    
    @Override
    @Transactional
    public void registrarConRol(Usuario usuario, RolNombre rolNombre) {
        
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede estar vacío");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new ContrasenaVaciaException("La contraseña no puede estar vacía");
        }

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }

        // Validación de formato de correo electrónico
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new EmailInvalidoException("El correo electrónico no tiene un formato válido");
        }

        if (existeUsuario(usuario.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailYaRegistradoException("El correo electrónico ya está en uso");
        }

        // Validación de la contraseña nueva (por ejemplo, longitud mínima)
        if (usuario.getPassword().length() < 6) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener al menos 6 caracteres.");
        }
        
        // Validación de la contraseña nueva (por ejemplo, longitud maxima)
        if (usuario.getPassword().length() > 16) { 
            throw new PasswordInvalidaException("La contraseña no cumple con los requisitos. Debe tener como maximo 16 caracteres.");
        }
        
        if (usuario.getUsername().equals("forzar-error")){ // SOLO PRUEBAS
            throw new RuntimeException("Error interno en el servidor");
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
