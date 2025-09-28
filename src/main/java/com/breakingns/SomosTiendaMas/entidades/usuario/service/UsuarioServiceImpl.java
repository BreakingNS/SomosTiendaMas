package com.breakingns.SomosTiendaMas.entidades.usuario.service;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.ActualizarUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.ContrasenaVaciaException;
import com.breakingns.SomosTiendaMas.security.exception.EmailInvalidoException;
import com.breakingns.SomosTiendaMas.security.exception.EmailYaRegistradoException;
import com.breakingns.SomosTiendaMas.security.exception.NombreUsuarioVacioException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordInvalidaException;
import com.breakingns.SomosTiendaMas.security.exception.RolNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioYaExisteException;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import jakarta.transaction.Transactional;

import java.util.List;
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
    private final LoginAttemptService loginAttemptService;
    private final ISesionActivaRepository sesionActivaRepository;

    public UsuarioServiceImpl(CarritoService carritoService, 
                                RolService rolService, 
                                IUsuarioRepository usuarioRepository, 
                                PasswordEncoder passwordEncoder, 
                                LoginAttemptService loginAttemptService,
                                ISesionActivaRepository sesionActivaRepository) {
        this.carritoService = carritoService;
        this.rolService = rolService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.sesionActivaRepository = sesionActivaRepository;   
    }
    
    @Override
    @Transactional
    public Usuario registrar(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }
    
    @Override
    @Transactional
    public Long registrarConRolDesdeDTO(RegistroUsuarioDTO dto, String ip) {
        String email = dto.getEmail();

        if (loginAttemptService.isBlocked(email, ip)) {
            throw new TooManyRequestsException("Demasiados intentos de registro desde esta IP/email. Intenta más tarde.");
        }

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

        // Validación: username no puede tener secuencias especiales como ._, -., .-, _., -_
        if (dto.getUsername().contains("._") || dto.getUsername().contains("-.") ||
            dto.getUsername().contains(".-") || dto.getUsername().contains("_.") ||
            dto.getUsername().contains("-_") || dto.getUsername().contains("_-")) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede contener secuencias como ._, -., .-, _., -_ o _-.");
        }

        // Validación: contraseña debe tener al menos una letra y un número
        if (!dto.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos una letra y un número.");
        }

        // Validación: contraseña no puede ser igual al nombre de usuario
        if (dto.getPassword().equals(dto.getUsername())) {
            throw new PasswordInvalidaException("La contraseña no puede ser igual al nombre de usuario.");
        }

        // Validación: email debe tener formato válido (más estricto)
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new EmailInvalidoException("El correo electrónico no tiene un formato válido.");
        }

        // Validación: username solo puede tener letras, números, guion bajo y punto
        if (!dto.getUsername().matches("^[A-Za-z0-9._-]{6,16}$")) {
            throw new NombreUsuarioVacioException("El nombre de usuario solo puede contener letras, números, guion bajo, guion medio y punto, y tener entre 6 y 16 caracteres.");
        }

        // Validación: username no puede tener dos puntos o dos guiones bajos seguidos
        if (dto.getUsername().contains("..") || dto.getUsername().contains("__") || dto.getUsername().contains("--")) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede tener dos puntos, dos guion medio o dos guiones bajos seguidos.");
        }

        // Validación: username no puede empezar ni terminar con punto o guion bajo
        if (dto.getUsername().startsWith(".") || dto.getUsername().startsWith("_") || dto.getUsername().startsWith("-") ||
            dto.getUsername().endsWith(".") || dto.getUsername().endsWith("_") || dto.getUsername().endsWith("-")) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede empezar ni terminar con punto, guion medio o guion bajo.");
        }

            // Permitir rol dinámico desde el DTO, por defecto ROLE_USUARIO
            RolNombre rolNombre;
            if (dto.getRol() != null && !dto.getRol().isBlank()) {
                try {
                    rolNombre = RolNombre.valueOf(dto.getRol());
                } catch (IllegalArgumentException ex) {
                    throw new RolNoEncontradoException("Rol especificado no válido: " + dto.getRol());
                }
            } else {
                rolNombre = RolNombre.ROLE_USUARIO;
            }

            Rol rol = rolService.getByNombre(rolNombre)
                    .orElseThrow(() -> new RolNoEncontradoException("Error: Rol no encontrado."));
        
        try {
            // Lógica real de registro (crear usuario, hashear password, etc.)
            
            Usuario usuario = new Usuario();
            usuario.setUsername(dto.getUsername());
            usuario.setPassword(dto.getPassword()); // Asegurate que después se encripta
            usuario.setEmail(dto.getEmail());
            usuario.setRol(rol);
            usuario.setNombreResponsable(dto.getNombreResponsable());
            usuario.setApellidoResponsable(dto.getApellidoResponsable());
            usuario.setDocumentoResponsable(dto.getDocumentoResponsable());
            usuario.setTipoUsuario(Usuario.TipoUsuario.valueOf(dto.getTipoUsuario()));
            usuario.setAceptaPoliticaPriv(dto.getAceptaPoliticaPriv());
            usuario.setAceptaTerminos(dto.getAceptaTerminos());
            usuario.setFechaNacimientoResponsable(dto.getFechaNacimientoResponsable());
            usuario.setGeneroResponsable(dto.getGeneroResponsable() != null ? Usuario.Genero.valueOf(dto.getGeneroResponsable()) : null);
            usuario.setIdioma(dto.getIdioma());
            usuario.setTimezone(dto.getTimezone());
            usuario.setActivo(true);
            usuario.setEmailVerificado(false);
            usuario.setFechaRegistro(java.time.LocalDateTime.now());
            usuario.setIntentosFallidosLogin(0);
            usuario.setCuentaBloqueada(false);
            usuario.setFechaUltimaModificacion(java.time.LocalDateTime.now());

            registrar(usuario);
            carritoService.crearCarrito(usuario.getIdUsuario());

            loginAttemptService.loginSucceeded(email, ip); // Limpia si hay intento previo

            return usuario.getIdUsuario();
        } catch (Exception e) {
            loginAttemptService.loginFailed(email, ip);
            throw e; // volver a lanzar para que sea manejado por el controller
        }
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

        usuario.setRol(rol);
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

        usuario.setRol(null);
        registrar(usuario);
        carritoService.crearCarrito(usuario.getIdUsuario());
    }

    @Override
    @Transactional
    public void actualizarUsuario(Long id, ActualizarUsuarioDTO usuarioDTO, Long id2) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));

        // Actualiza los campos básicos
        if (usuarioDTO.getUsername() != null) usuario.setUsername(usuarioDTO.getUsername());
        if (usuarioDTO.getEmail() != null) usuario.setEmail(usuarioDTO.getEmail());
        if (usuarioDTO.getNombreResponsable() != null) usuario.setNombreResponsable(usuarioDTO.getNombreResponsable());
        if (usuarioDTO.getApellidoResponsable() != null) usuario.setApellidoResponsable(usuarioDTO.getApellidoResponsable());
        if (usuarioDTO.getDocumentoResponsable() != null) usuario.setDocumentoResponsable(usuarioDTO.getDocumentoResponsable());
        if (usuarioDTO.getFechaNacimientoResponsable() != null) usuario.setFechaNacimientoResponsable(usuarioDTO.getFechaNacimientoResponsable());
        if (usuarioDTO.getGeneroResponsable() != null) usuario.setGeneroResponsable(usuarioDTO.getGeneroResponsable());
        if (usuarioDTO.getIdioma() != null) usuario.setIdioma(usuarioDTO.getIdioma());
        if (usuarioDTO.getTimezone() != null) usuario.setTimezone(usuarioDTO.getTimezone());
        usuario.setFechaUltimaModificacion(java.time.LocalDateTime.now());

        // Si se envía una nueva contraseña, la actualiza y la encripta
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }

        usuarioRepository.save(usuario);
    }

    public UsuarioResponseDTO consultarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombreResponsable(usuario.getNombreResponsable());
        dto.setApellidoResponsable(usuario.getApellidoResponsable());
        dto.setDocumentoResponsable(usuario.getDocumentoResponsable());
        dto.setTipoUsuario(usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().name() : null);
        dto.setIdioma(usuario.getIdioma());
        dto.setTimezone(usuario.getTimezone());
        dto.setActivo(usuario.getActivo());
        dto.setEmailVerificado(usuario.getEmailVerificado());
        // Agrega otros campos relevantes si es necesario
        return dto;
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }

    // Metodos Privados
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

    @Override
    public List<Usuario> traerTodoUsuario() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional
    public void desactivarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false); // o usuario.setEstado("INACTIVO");
        usuarioRepository.save(usuario);

        // Opcional: invalidar sesiones activas
        sesionActivaRepository.deleteByUsuario_IdUsuario(idUsuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {
        List<Usuario> usuarios = traerTodoUsuario();
        return usuarios.stream().map(u -> {
            UsuarioResponseDTO dto = new UsuarioResponseDTO();
            dto.setIdUsuario(u.getIdUsuario());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setNombreResponsable(u.getNombreResponsable());
            dto.setApellidoResponsable(u.getApellidoResponsable());
            dto.setDocumentoResponsable(u.getDocumentoResponsable());
            dto.setTipoUsuario(u.getTipoUsuario() != null ? u.getTipoUsuario().name() : null);
            dto.setIdioma(u.getIdioma());
            dto.setTimezone(u.getTimezone());
            dto.setActivo(u.getActivo());
            dto.setEmailVerificado(u.getEmailVerificado());
            // añade más campos si UsuarioResponseDTO los tiene
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuarioParcial(Long id, ActualizarUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado con id: " + id));

        // aplicar solo campos no nulos
        if (dto.getUsername() != null) usuario.setUsername(dto.getUsername());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getNombreResponsable() != null) usuario.setNombreResponsable(dto.getNombreResponsable());
        if (dto.getApellidoResponsable() != null) usuario.setApellidoResponsable(dto.getApellidoResponsable());
        if (dto.getDocumentoResponsable() != null) usuario.setDocumentoResponsable(dto.getDocumentoResponsable());
        if (dto.getFechaNacimientoResponsable() != null) usuario.setFechaNacimientoResponsable(dto.getFechaNacimientoResponsable());
        if (dto.getGeneroResponsable() != null) usuario.setGeneroResponsable(dto.getGeneroResponsable());
        if (dto.getIdioma() != null) usuario.setIdioma(dto.getIdioma());
        if (dto.getTimezone() != null) usuario.setTimezone(dto.getTimezone());
        // Si se envía nueva contraseña, encriptarla
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario.setFechaUltimaModificacion(java.time.LocalDateTime.now());

        usuarioRepository.save(usuario);

        // devolver DTO actualizado (reutiliza consultarUsuario para consistencia)
        return consultarUsuario(usuario.getIdUsuario());
    }
}
