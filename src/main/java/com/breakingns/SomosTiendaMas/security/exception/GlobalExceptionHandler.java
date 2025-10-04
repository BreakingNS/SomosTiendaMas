package com.breakingns.SomosTiendaMas.security.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.BindException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private Map<String, Object> body(int status, String message) {
        return Map.of(
            "timestamp", Instant.now().toString(),
            "status", status,
            "message", message
        );
    }
    /* 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,Object> body = Map.of(
            "timestamp", Instant.now().toString(),
            "status", 400,
            "message", "Validación inválida",
            "errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .collect(Collectors.toList())
        );
        return ResponseEntity.badRequest().body(body);
    }*/

    @ExceptionHandler(PerfilEmpresaNoEncontradoException.class)
    public ResponseEntity<Map<String,Object>> handlePerfilEmpresaNoEncontrado(PerfilEmpresaNoEncontradoException ex) {
        Map<String,Object> body = Map.of(
            "timestamp", java.time.Instant.now().toString(),
            "status", 404,
            "message", ex.getMessage()
        );
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", ex.getMessage() == null ? "error inesperado" : ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        // opcional: incluir detalles de media types supportados
        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, Object>> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_ACCEPTABLE.value());
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());

        String param = ex.getName();
        String value = String.valueOf(ex.getValue()); // seguro para null
        Class<?> expectedType = ex.getRequiredType();
        String expected = expectedType != null ? expectedType.getSimpleName() : "desconocido";

        body.put("message", String.format("Parámetro '%s' inválido: valor '%s' (esperado: %s)", param, value, expected));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ ConversionFailedException.class, NumberFormatException.class })
    public ResponseEntity<Map<String, Object>> handleConversionFailure(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Error de conversión de parámetros: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    // Un único handler que cubre MethodArgumentNotValidException y BindException (validación de @RequestBody y form binding)
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<Map<String, Object>> handleValidationErrors(Exception ex) {
        String msg;
        if (ex instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b).orElse("Payload inválido");
        } else {
            msg = ((BindException) ex).getBindingResult().getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b).orElse("Payload inválido");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handler para ConstraintViolationException (por ejemplo validaciones en @RequestParam/@PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Parámetros inválidos");
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ OwnerNotFoundException.class })
    public ResponseEntity<Map<String, Object>> handleOwnerNotFound(OwnerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler({ EntityNotFoundException.class })
    public ResponseEntity<Map<String, Object>> handleJakartaEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }
    /*
    // handler genérico (dejar al final)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }*/

    @ExceptionHandler(InvalidDireccionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDireccion(InvalidDireccionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
    /*
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Payload inválido");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST.value(), msg));
    }*/

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno"));
    }*/

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
    /* 
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Argumento ilegal",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }
    */

    @ExceptionHandler(SesionExpiradaException.class)
    public ResponseEntity<?> handleSesionExpirada(SesionExpiradaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Sesión expirada",
                    "message", ex.getMessage(),
                    "timestamp", java.time.Instant.now().toString()
                ));
    }

    @ExceptionHandler(EmailNoVerificadoException.class)
    public ResponseEntity<?> handleEmailNoVerificadoException(EmailNoVerificadoException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Recuperación de contraseña",
            "message", ex.getMessage(),
            "timestamp", java.time.Instant.now().toString(),
            "status", 400
        ));
    }

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<?> handleEmailVerificationException(EmailVerificationException ex) {
        ex.printStackTrace(); // Esto imprime la excepción en consola
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Verificación de email",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value()
        ));
    }

    @ExceptionHandler(UsuarioDesactivadoException.class)
    public ResponseEntity<?> handleUsuarioDesactivadoException(UsuarioDesactivadoException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", ex.getMessage()));
    }
    /* 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.toList());

        Map<String, List<String>> respuesta = new HashMap<>();
        respuesta.put("messages", errores);

        return ResponseEntity.badRequest().body(respuesta);
    }*/

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
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
    }

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

    @ExceptionHandler(TokenRevocadoException.class)
    public ResponseEntity<?> manejarTokenRevocado(TokenRevocadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Token revocado",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "error", "Token Inválido",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.UNAUTHORIZED.value()
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
    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarExcepcionesGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Error interno",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString(),
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        ));
    }*/

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