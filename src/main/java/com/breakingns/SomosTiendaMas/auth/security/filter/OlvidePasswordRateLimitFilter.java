package com.breakingns.SomosTiendaMas.auth.security.filter;

import com.breakingns.SomosTiendaMas.security.rate.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OlvidePasswordRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiter;

    public OlvidePasswordRateLimitFilter(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("/public/olvide-password".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = request.getRemoteAddr();

            if (rateLimiter.isBlocked(ip)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"mensaje\": \"Demasiados intentos. Por favor, espera un momento.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}