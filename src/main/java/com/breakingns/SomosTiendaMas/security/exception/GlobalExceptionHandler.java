package com.breakingns.SomosTiendaMas.security.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
    
    @ExceptionHandler(UsuarioBloqueadoException.class)
    public ResponseEntity<?> manejarUsuarioBloqueado(UsuarioBloqueadoException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "TOO_MANY_REQUESTS");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
    
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<?> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "UNAUTHORIZED");
        body.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
    
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequests(TooManyRequestsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }
    
    @ExceptionHandler(SesionNoValidaException.class)
    public ResponseEntity<String> manejarSesionNoValidaException(SesionNoValidaException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(SesionActivaNoEncontradaException.class)
    public ResponseEntity<String> manejarSesionActivaNoEncontradaException(SesionActivaNoEncontradaException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<?> handleTokenException(TokenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "error", "Token Inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now(),
            "status", HttpStatus.FORBIDDEN.value()
        ));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        // Puedes personalizar el mensaje o estructura de la respuesta según lo que necesites
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", ex.getMessage());  // Esto es lo que estás buscando
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(EmailInvalidoException.class)
    public ResponseEntity<String> handleEmailInvalidoException(EmailInvalidoException e) {
        return ResponseEntity.badRequest().body("{\"mensaje\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(NombreUsuarioVacioException.class)
    public ResponseEntity<String> handleNombreUsuarioVacioException(NombreUsuarioVacioException e) {
        return ResponseEntity.badRequest().body("{\"mensaje\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(ContrasenaVaciaException.class)
    public ResponseEntity<String> handleContrasenaVaciaException(ContrasenaVaciaException e) {
        return ResponseEntity.badRequest().body("{\"mensaje\": \"" + e.getMessage() + "\"}");
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<String> handleEmailYaRegistradoException(EmailYaRegistradoException e) {
        return ResponseEntity.badRequest().body("{\"mensaje\": \"" + e.getMessage() + "\"}");
    }
    
    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<?> manejarTokenExpirado(TokenExpiradoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Token expirado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(TokenYaUsadoException.class)
    public ResponseEntity<?> manejarTokenYaUsado(TokenYaUsadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Token usado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }
    
    @ExceptionHandler(PasswordIgualAAnteriorException.class)
    public ResponseEntity<?> handlePasswordIgualAAnterior(PasswordIgualAAnteriorException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Contraseña repetida",
            "message", ex.getMessage(),
            "timestamp", Instant.now()
        ));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Error de validación.");
        return ResponseEntity.badRequest().body(Map.of("message", mensaje));
    }
    
    @ExceptionHandler(PasswordInvalidaException.class)
    public ResponseEntity<?> manejarPasswordInvalida(PasswordInvalidaException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(respuesta);
    }
    
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenException(RefreshTokenException ex) {
        System.out.println(">>>>> Entró al handler de RefreshTokenException");
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Token Inválido");
        body.put("message", ex.getMessage()); // <-- clave esperada por el test
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<?> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(TokenNoEncontradoException.class)
    public ResponseEntity<?> manejarTokenNoEncontrado(TokenNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "error", "Token no encontrado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }
    
    @ExceptionHandler(PrincipalInvalidoException.class)
    public ResponseEntity<?> handlePrincipalInvalido(PrincipalInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(PasswordIncorrectaException.class)
    public ResponseEntity<?> manejarPasswordIncorrecta(PasswordIncorrectaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "error", "Contraseña incorrecta",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> manejarUsernameNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Usuario no encontrado",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> manejarAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Acceso denegado",
                "message", "No tenés permisos para acceder a este recurso.",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<?> handleUsuarioYaExisteException(UsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Usuario duplicado",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Recurso no encontrado",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarExcepcionesGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error interno",
                "message", ex.getMessage(),
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
                "message", ex.getMessage(),
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