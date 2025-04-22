package com.breakingns.SomosTiendaMas.auth.security.filter;

import com.breakingns.SomosTiendaMas.auth.repository.ITokenBlacklistRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
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
import org.springframework.util.AntPathMatcher;

//Es un filtro de seguridad personalizado que se ejecuta una vez por cada request (OncePerRequestFilter)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final ITokenBlacklistRepository tokenBlacklistRepository;
    
    //Lista de rutas públicas
    private static final List<String> RUTAS_PUBLICAS = List.of(
        "/api/auth/**",
        "/api/usuarios/registro/**"
    );
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


    //Constructor
        public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                   UserDetailsServiceImpl userDetailsService,
                                   ITokenBlacklistRepository tokenBlacklistRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }
    
    @Override
    //Metodo principal: Este método se ejecuta por cada request que entra.
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        //Verifica si es una ruta pública
        String path = request.getRequestURI();

        for (String rutaPublica : RUTAS_PUBLICAS) {
            if (pathMatcher.match(rutaPublica, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        //Verifica que venga el header Authorization
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token faltante o mal formado");
            return;
        }

        //Extrae y valida el token
        String token = header.substring(7);

        if (!jwtTokenProvider.validarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o expirado");
            return;
        }

        //Validar contra blacklist
        if (tokenBlacklistRepository.existsByToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token revocado (blacklist)");
            return;
        }

        //Obtiene el usuario desde el token
        String username = jwtTokenProvider.obtenerUsernameDelToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //Crea el objeto de autenticación
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);//Guarda la autenticación en el contexto de seguridad de Spring para que esté disponible mientras dure la sesión de esa request
        
        //Deja pasar la request
        filterChain.doFilter(request, response);
    }
    /*
    @Override
    //Metodo principal: Este método se ejecuta por cada request que entra.
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        //Verifica si es una ruta pública
        String path = request.getRequestURI();

        for (String rutaPublica : RUTAS_PUBLICAS) {
            if (pathMatcher.match(rutaPublica, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        //Verifica que venga el header Authorization
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token faltante o mal formado");
            return;
        }

        //Extrae y valida el token
        String token = header.substring(7);

        if (!jwtTokenProvider.validarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido o expirado");
            return;
        }

        //Obtiene el usuario desde el token
        String username = jwtTokenProvider.obtenerUsernameDelToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //Crea el objeto de autenticación
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);//Guarda la autenticación en el contexto de seguridad de Spring para que esté disponible mientras dure la sesión de esa request
        
        //Deja pasar la request
        filterChain.doFilter(request, response);
    }
    */
}
