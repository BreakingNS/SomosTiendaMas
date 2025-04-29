package com.breakingns.SomosTiendaMas.security.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<?> manejarRefreshTokenException(RefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Token Inválido",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> manejarUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Usuario no encontrado",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> manejarAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Acceso denegado",
                "mensaje", "No tenés permisos para acceder a este recurso.",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarExcepcionesGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error interno",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    // Maneja excepciones personalizadas de tipo UsuarioYaExisteException
    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<?> handleUsuarioYaExisteException(UsuarioYaExisteException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Código de respuesta 400
                .body("El nombre de usuario ya está en uso: " + ex.getMessage());
    }

    // Maneja excepciones genéricas (Exception)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // Código de respuesta 500
                .body("Ocurrió un error inesperado: " + ex.getMessage());
    }

    // Maneja NotFoundException, devuelve 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Recurso no encontrado: " + ex.getMessage());
    }

    // Manejo de excepciones de validación (por ejemplo: @Valid en el controller)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getAllErrors().forEach(error -> 
            errorMessage.append(error.getDefaultMessage()).append(" ")
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage.toString());
    }
}