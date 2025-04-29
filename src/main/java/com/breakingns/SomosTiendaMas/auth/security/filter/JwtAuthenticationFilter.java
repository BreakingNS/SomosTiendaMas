package com.breakingns.SomosTiendaMas.auth.security.filter;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
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
import java.util.Optional;
import org.springframework.util.AntPathMatcher;

//Es un filtro de seguridad personalizado que se ejecuta una vez por cada request (OncePerRequestFilter)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    //Lista de rutas públicas
    private static final List<String> RUTAS_PUBLICAS = List.of(
        "/api/auth/public/**"
    );

    //Constructor
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                   UserDetailsServiceImpl userDetailsService,
                                   ITokenEmitidoRepository tokenEmitidoRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
    }
    
    @Override//SE USA ---
    //Metodo principal: Este método se ejecuta por cada request que entra.//SE USA ---
    protected void doFilterInternal(HttpServletRequest request,//SE USA ---
                                    HttpServletResponse response,//SE USA ---
                                    FilterChain filterChain) //SE USA ---
            throws ServletException, IOException {//SE USA ---
        
        System.out.println("Ejecutando JwtAuthenticationFilter...");
        
        //Verifica si es una ruta pública
        //String path = request.getRequestURI();
        String path = request.getServletPath();
        
        System.out.println("Verificando token para la ruta: " + request.getRequestURI());
        
        for (String rutaPublica : RUTAS_PUBLICAS) {
            if (pathMatcher.match(rutaPublica, path)) {
                System.out.println("Ruta pública detectada: " + path); // Log para detectar si es una ruta pública
                filterChain.doFilter(request, response);
                return;
            }
        }

        //Verifica que venga el header Authorization
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token faltante o mal formado");
            System.out.println("Header Authorization recibido: " + header);
            return;
        }

        //Extrae y valida el token
        String token = header.substring(7);

        if (!jwtTokenProvider.validarToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o expirado");
            
            return;
        }
        
        //Validar contra tabla token_emitido
        Optional <TokenEmitido> tokenDb = tokenEmitidoRepository.findByToken(token);
        if (tokenDb.isEmpty() || tokenDb.get().isRevocado()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido o revocado");
            return;
        }
        
        //Obtiene el usuario desde el token
        String username = jwtTokenProvider.obtenerUsernameDelToken(token);
        System.out.println("Username extraído del token: " + username);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        System.out.println("UserDetails cargado: " + userDetails);
        
        System.out.println("por Autenticar");
        //Crea el objeto de autenticación
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        System.out.println("supongo que Autenticado");
        SecurityContextHolder.getContext().setAuthentication(authToken);//Guarda la autenticación en el contexto de seguridad de Spring para que esté disponible mientras dure la sesión de esa request
        System.out.println("paso el SecurityContextHolder");
        //Deja pasar la request
        System.out.println("por entrar al doFilter");
        filterChain.doFilter(request, response);
    }
    
}
