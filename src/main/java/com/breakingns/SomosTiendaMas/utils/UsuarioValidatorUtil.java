package com.breakingns.SomosTiendaMas.utils;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.ContrasenaVaciaException;
import com.breakingns.SomosTiendaMas.security.exception.EmailInvalidoException;
import com.breakingns.SomosTiendaMas.security.exception.EmailYaRegistradoException;
import com.breakingns.SomosTiendaMas.security.exception.NombreUsuarioVacioException;
import com.breakingns.SomosTiendaMas.security.exception.PasswordInvalidaException;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioYaExisteException;

public class UsuarioValidatorUtil {

    public static void validarRegistroUsuario(RegistroUsuarioDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new NombreUsuarioVacioException("El nombre de usuario no puede estar vacío");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ContrasenaVaciaException("La contraseña no puede estar vacía");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new EmailInvalidoException("El correo electrónico no tiene un formato válido.");
        }
        if (dto.getPassword().length() < 8 || dto.getPassword().length() > 128) {
            throw new PasswordInvalidaException("La contraseña debe tener entre 8 y 128 caracteres.");
        }
        if (!dto.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new PasswordInvalidaException("La contraseña debe contener al menos una letra y un número.");
        }
        if (dto.getPassword().equals(dto.getUsername())) {
            throw new PasswordInvalidaException("La contraseña no puede ser igual al nombre de usuario.");
        }
        if (!dto.getUsername().matches("^[A-Za-z0-9._-]{8,128}$")) {
            throw new NombreUsuarioVacioException("El nombre de usuario solo puede contener letras, números, guion bajo, guion medio y punto, y tener entre 8 y 128 caracteres.");
        }
        
        // Validaciones extra para los campos nuevos
        if (dto.getNombreResponsable() == null || dto.getNombreResponsable().isBlank()) {
            throw new IllegalArgumentException("El nombre del responsable no puede estar vacío");
        }
        if (dto.getApellidoResponsable() == null || dto.getApellidoResponsable().isBlank()) {
            throw new IllegalArgumentException("El apellido del responsable no puede estar vacío");
        }
        if (dto.getDocumentoResponsable() == null || dto.getDocumentoResponsable().isBlank()) {
            throw new IllegalArgumentException("El documento del responsable no puede estar vacío");
        } 
        if (dto.getTipoUsuario() == null || dto.getTipoUsuario().isBlank()) {
            throw new IllegalArgumentException("El tipo de usuario es obligatorio");
        }
        if (dto.getAceptaTerminos() == null || !dto.getAceptaTerminos()) {
            throw new IllegalArgumentException("Debe aceptar los términos y condiciones");
        }
        if (dto.getAceptaPoliticaPriv() == null || !dto.getAceptaPoliticaPriv()) {
            throw new IllegalArgumentException("Debe aceptar la política de privacidad");
        }
        // Puedes agregar más validaciones según tu lógica de negocio
    }

    public static void validarExistenciasUsuario(RegistroUsuarioDTO dto, IUsuarioRepository usuarioRepository) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailYaRegistradoException("El correo electrónico ya está en uso");
        }
        // Puedes agregar más validaciones de unicidad si lo necesitas
    }

    public static void validarIntentosRegistro(String email, String ip, LoginAttemptService loginAttemptService) {
        if (loginAttemptService.isBlocked(email, ip)) {
            throw new TooManyRequestsException("Demasiados intentos de registro desde esta IP/email. Intenta más tarde.");
        }
    }

}