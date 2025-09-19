package com.breakingns.SomosTiendaMas.security.filter;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OlvidePasswordRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService rateLimiter;

    public OlvidePasswordRateLimitFilter(LoginAttemptService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("/public/olvide-password".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) ip = request.getRemoteAddr();

            // Leer el cuerpo del request para obtener el email
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : "";

            if (rateLimiter.isBlocked(email, ip)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Demasiadas solicitudes, intenta más tarde.\"}");
                return;
            }

            // El body ya se leyó, hay que reinyectarlo para los siguientes filtros o controllers
            HttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, body);
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}