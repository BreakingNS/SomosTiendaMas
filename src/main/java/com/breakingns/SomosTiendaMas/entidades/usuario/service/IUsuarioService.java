package com.breakingns.SomosTiendaMas.entidades.usuario.service;

import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.ActualizarUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    
    Optional<Usuario> findById(Long id);
    
    Usuario registrar(Usuario usuario);
    
    Boolean existeUsuario(String nombreUsuario);
    
    Usuario findByUsername(String username);
    
    void registrarConRol(Usuario usuario, RolNombre rolNombre);
    
    void registrarSinRol(Usuario usuario); // SOLO PRUEBA, no produccion
    
    //void registrarConRolDesdeDTO(RegistroUsuarioDTO dto, RolNombre rolNombre);
    
    Long registrarConRolDesdeDTO(RegistroUsuarioDTO registroDTO, String ip);

    void actualizarUsuario(Long id, ActualizarUsuarioDTO usuarioDTO, Long id2);

    List<Usuario> traerTodoUsuario();

    void desactivarUsuario(Long id);

    // ya existentes pueden tener otros nombres; añadir/normalizar estas
    UsuarioResponseDTO consultarUsuario(Long id);

    List<UsuarioResponseDTO> listarUsuarios();

    UsuarioResponseDTO actualizarUsuarioParcial(Long id, ActualizarUsuarioDTO dto);

    void eliminarUsuario(Long id);
    
}
