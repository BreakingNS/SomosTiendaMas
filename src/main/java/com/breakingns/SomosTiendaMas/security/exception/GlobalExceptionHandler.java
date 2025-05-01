package com.breakingns.SomosTiendaMas.security.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<?> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TokenNoEncontradoException.class)
    public ResponseEntity<?> handleTokenNoEncontrado(TokenNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PrincipalInvalidoException.class)
    public ResponseEntity<?> handlePrincipalInvalido(PrincipalInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(PasswordIncorrectaException.class)
    public ResponseEntity<?> manejarPasswordIncorrecta(PasswordIncorrectaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Contraseña incorrecta",
            "mensaje", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }
    
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

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<?> handleUsuarioYaExisteException(UsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Usuario duplicado",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Recurso no encontrado",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getAllErrors().forEach(error ->
            errorMessage.append(error.getDefaultMessage()).append(" ")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Validación fallida",
                "mensaje", errorMessage.toString().trim(),
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
    
    @ExceptionHandler(TokenResetPasswordInvalidoException.class)
    public ResponseEntity<?> manejarTokenResetPasswordInvalido(TokenResetPasswordInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Token inválido",
            "mensaje", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }
    
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<?> manejarTokenInvalido(TokenInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }
    
    
    @ExceptionHandler(SesionNoEncontradaException.class)
    public ResponseEntity<?> manejarSesionNoEncontrada(SesionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<?> manejarAccesoDenegado(AccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> manejarAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Autenticación fallida",
                "mensaje", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }
    
    /*
    
    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<?> manejarTokenExpirado(TokenExpiradoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CarritoNoEncontradoException.class)
    public ResponseEntity<?> manejarCarritoNoEncontrado(CarritoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // Por si querés un catch-all opcional
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarExcepcionGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error inesperado: " + ex.getMessage()));
    }
    */
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RolNoEncontradoException.class)
    public ResponseEntity<String> handleRolNoEncontrado(RolNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
    }
}