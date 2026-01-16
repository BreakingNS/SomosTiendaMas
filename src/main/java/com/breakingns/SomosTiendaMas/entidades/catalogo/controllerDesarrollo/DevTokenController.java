package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

//import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import java.util.Comparator;

// TODO: eliminar este controlador en producción

@RestController
@RequestMapping("/dev")
//@Profile("dev") // sólo activo con spring.profiles.active=dev
public class DevTokenController {

    private final ITokenEmitidoRepository tokenRepo;

    public DevTokenController(ITokenEmitidoRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String,String>> getToken(HttpServletRequest req) {
        // 1) intentar header Authorization
        String token = "";
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }

        // 2) si no vino en header, intentar buscar en cookies (algunas integraciones colocan el token allí)
        if ((token == null || token.isEmpty()) && req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                String name = c.getName();
                String v = c.getValue();
                if (v == null) continue;
                if ("Authorization".equalsIgnoreCase(name) || "authorization".equalsIgnoreCase(name)) {
                    if (v.startsWith("Bearer ")) { token = v.substring(7); break; }
                }
                // alternativa: cookie llamada "jwt" o "access_token"
                if ("jwt".equalsIgnoreCase(name) || "access_token".equalsIgnoreCase(name) || "token".equalsIgnoreCase(name)) {
                    token = v; break;
                }
            }
        }

        // 3) si aún vacío, intentar obtener credenciales desde el SecurityContext (si Spring Security almacena credentials)
        if (token == null || token.isEmpty()) {
            try {
                Authentication authn = SecurityContextHolder.getContext().getAuthentication();
                if (authn != null) {
                    Object creds = authn.getCredentials();
                    if (creds != null) token = creds.toString();
                }
            } catch (Exception e) {
                // ignore - sólo intento de fallback para entorno dev
            }
        }

        return ResponseEntity.ok(Collections.singletonMap("jwt", token == null ? "" : token));
    }

    /**
     * Devuelve el token activo más reciente para el usuario dado (solo para desarrollo).
     * Ejemplo: GET /dev/token/user/1 -> { "jwt": "..." }
     */
    @GetMapping("/token/user/{userId}")
    public ResponseEntity<Map<String,String>> getTokenForUser(@PathVariable("userId") Long userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body(Collections.singletonMap("jwt", ""));
            var tokens = tokenRepo.findAllByUsuario_IdUsuarioAndRevocadoFalse(userId);
            if (tokens == null || tokens.isEmpty()) return ResponseEntity.ok(Collections.singletonMap("jwt", ""));
                TokenEmitido chosen = tokens.stream()
                    .max(Comparator.comparing(TokenEmitido::getFechaExpiracion))
                    .orElse(tokens.get(0));
            return ResponseEntity.ok(Collections.singletonMap("jwt", chosen.getToken()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("jwt", ""));
        }
    }

    /**
     * Devuelve el token activo más reciente para el usuario que realizó la solicitud.
     * Extrae el id del `SecurityContext` si está disponible; como fallback intenta
     * buscar por el token enviado en el header Authorization.
     */
    @GetMapping("/token/me")
    public ResponseEntity<Map<String,String>> getTokenForMe(HttpServletRequest req) {
        try {
            Long userId = null;

            // 1) intentar extraer id desde SecurityContext principal
            try {
                Authentication authn = SecurityContextHolder.getContext().getAuthentication();
                if (authn != null && authn.getPrincipal() instanceof UserAuthDetails) {
                    userId = ((UserAuthDetails) authn.getPrincipal()).getId();
                }
            } catch (Exception ignored) {}

            // 2) fallback: si no hay principal, intentar obtener token del header y buscar su owner
            if (userId == null) {
                String auth = req.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7);
                    var opt = tokenRepo.findByToken(token);
                    if (opt.isPresent()) {
                        TokenEmitido te = opt.get();
                        Usuario u = te.getUsuario();
                        if (u != null) userId = u.getIdUsuario();
                    }
                }
            }

            if (userId == null) {
                return ResponseEntity.status(403).body(Collections.singletonMap("jwt", ""));
            }

            var tokens = tokenRepo.findAllByUsuario_IdUsuarioAndRevocadoFalse(userId);
            if (tokens == null || tokens.isEmpty()) return ResponseEntity.ok(Collections.singletonMap("jwt", ""));
            TokenEmitido chosen = tokens.stream()
                    .max(Comparator.comparing(TokenEmitido::getFechaExpiracion))
                    .orElse(tokens.get(0));
            return ResponseEntity.ok(Collections.singletonMap("jwt", chosen.getToken()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("jwt", ""));
        }
    }
}