package com.breakingns.SomosTiendaMas.auth.security.filter;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import com.breakingns.SomosTiendaMas.auth.utils.JwtAuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.AntPathMatcher;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    /*
            Filtros que realiza:

        - Filtra solo rutas privadas
        - Verifica el header Authorization y formato "Bearer "
        - Valida el token con jwtTokenProvider
        - Revisa si el token está revocado en la DB (TokenEmitido)
        - Extrae el usuario y autentica en el contexto de Spring
        - Manejo de errores inesperados (try-catch)
    
    */
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Rutas públicas
    private static final List<String> RUTAS_PUBLICAS = List.of("/api/auth/public/**");

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsServiceImpl userDetailsService,
                                   ITokenEmitidoRepository tokenEmitidoRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            for (String rutaPublica : RUTAS_PUBLICAS) {
                if (pathMatcher.match(rutaPublica, path)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                JwtAuthUtil.noAutenticado(response, "Token faltante o mal formado");
                return;
            }

            String token = header.substring(7);
            if (!jwtTokenProvider.validarToken(token)) {
                JwtAuthUtil.noAutorizado(response, "Token inválido o expirado");
                return;
            }

            Optional<TokenEmitido> tokenDb = tokenEmitidoRepository.findByToken(token);
            if (tokenDb.isEmpty() || tokenDb.get().isRevocado()) {
                JwtAuthUtil.noAutorizado(response, "Token inválido o revocado");
                return;
            }

            String username = jwtTokenProvider.obtenerUsernameDelToken(token);
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                JwtAuthUtil.rechazar(response, "Usuario no encontrado.");
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (ServletException | IOException e) {
            JwtAuthUtil.rechazar(response, "Error inesperado en autenticación.");
            System.err.println("Error en JwtAuthenticationFilter: " + e.getMessage());
        }
    }
}