package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.JwtResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    /*
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) { //recibe en el cuerpo del request (JSON) un objeto LoginRequest.
        //Autenticacion
        Authentication authentication = authenticationManager.authenticate( //intenta autenticar el usuario usando el Authenticacion Manager.
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        //Guardar la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication); //Esto guarda la autenticación en el contexto de seguridad de Spring para que esté disponible mientras dure la sesión de esa request
        
        //Generación del Token JWT
        String token = jwtTokenProvider.generarToken(authentication); //Llama a un método que genera un JWT (JSON Web Token) a partir del usuario autenticado.
        
        //Respuesta
        return ResponseEntity.ok(new JwtResponse(token)); //Devuelve una respuesta HTTP 200 con un objeto JwtResponse que contiene el token generado.
    }
}

