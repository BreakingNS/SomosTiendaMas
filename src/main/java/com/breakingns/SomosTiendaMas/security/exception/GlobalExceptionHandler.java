package com.breakingns.SomosTiendaMas.security.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.toList());

        Map<String, List<String>> respuesta = new HashMap<>();
        respuesta.put("messages", errores);

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabaseConnectionError(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "error", "Error de conexión a la base de datos",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value()
        ));
    }

    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<?> manejarUsuarioBloqueado(UsuarioBloqueadoException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
            "error", "TOO_MANY_REQUESTS",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.TOO_MANY_REQUESTS.value()
        ));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<?> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "UNAUTHORIZED",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
        ));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<?> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
            "error", "Too Many Requests",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.TOO_MANY_REQUESTS.value()
        ));
    }

    @ExceptionHandler(SesionNoValidaException.class)
    public ResponseEntity<?> manejarSesionNoValidaException(SesionNoValidaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Sesión no válida",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
        ));
    }

    @ExceptionHandler(SesionActivaNoEncontradaException.class)
    public ResponseEntity<?> manejarSesionActivaNoEncontradaException(SesionActivaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Sesión activa no encontrada",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<?> handleTokenException(TokenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Token Inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.FORBIDDEN.value()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = Map.of("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    
    /*
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Error interno",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }*/

    @ExceptionHandler(EmailInvalidoException.class)
    public ResponseEntity<?> handleEmailInvalidoException(EmailInvalidoException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Email inválido",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(NombreUsuarioVacioException.class)
    public ResponseEntity<?> handleNombreUsuarioVacioException(NombreUsuarioVacioException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Nombre de usuario vacío",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(ContrasenaVaciaException.class)
    public ResponseEntity<?> handleContrasenaVaciaException(ContrasenaVaciaException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Contraseña vacía",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<?> handleEmailYaRegistradoException(EmailYaRegistradoException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Email ya registrado",
            "message", e.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<?> manejarTokenExpirado(TokenExpiradoException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Token expirado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(TokenYaUsadoException.class)
    public ResponseEntity<?> manejarTokenYaUsado(TokenYaUsadoException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Token ya usado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(PasswordIgualAAnteriorException.class)
    public ResponseEntity<?> handlePasswordIgualAAnterior(PasswordIgualAAnteriorException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Contraseña repetida",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(PasswordInvalidaException.class)
    public ResponseEntity<?> manejarPasswordInvalida(PasswordInvalidaException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Contraseña inválida",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<?> handleRefreshTokenException(RefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Token Inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.FORBIDDEN.value()
        ));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<?> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Usuario no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(TokenNoEncontradoException.class)
    public ResponseEntity<?> manejarTokenNoEncontrado(TokenNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Token no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(PrincipalInvalidoException.class)
    public ResponseEntity<?> handlePrincipalInvalido(PrincipalInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Principal inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }

    @ExceptionHandler(PasswordIncorrectaException.class)
    public ResponseEntity<?> manejarPasswordIncorrecta(PasswordIncorrectaException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Contraseña incorrecta",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> manejarUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Usuario no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> manejarAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Acceso denegado",
            "message", "No tenés permisos para acceder a este recurso.",
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.FORBIDDEN.value()
        ));
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<?> handleUsuarioYaExisteException(UsuarioYaExisteException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Usuario duplicado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Recurso no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarExcepcionesGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Error interno",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<?> manejarTokenInvalido(TokenInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Token inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
        ));
    }

    @ExceptionHandler(SesionNoEncontradaException.class)
    public ResponseEntity<?> manejarSesionNoEncontrada(SesionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Sesión no encontrada",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<?> manejarAccesoDenegado(AccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Acceso denegado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.FORBIDDEN.value()
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> manejarAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Autenticación fallida",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
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
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Argumento ilegal",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(RolNoEncontradoException.class)
    public ResponseEntity<?> handleRolNoEncontrado(RolNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Rol no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Ruta no encontrada",
            "message", "La URL solicitada no existe",
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.NOT_FOUND.value()
        ));
    }
}