/*
package com.breakingns.SomosTiendaMas.auth.security.config;

import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.filter.JwtAuthenticationFilter;
import com.breakingns.SomosTiendaMas.auth.security.filter.OlvidePasswordRateLimitFilter;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint cCustomAuthenticationEntryPoint;

    public SecurityConfig(CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPoint cCustomAuthenticationEntryPoint) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.cCustomAuthenticationEntryPoint = cCustomAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                           UserDetailsServiceImpl userDetailsService,
                                                           ITokenEmitidoRepository tokenEmitidoRepository) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, tokenEmitidoRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               JwtAuthenticationFilter jwtAuthenticationFilter,
                                               OlvidePasswordRateLimitFilter rateLimitFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(cCustomAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PublicRoutes.RUTAS_PUBLICAS).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
            .build();
    }
}
    */

package com.breakingns.SomosTiendaMas.security.config;

import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.breakingns.SomosTiendaMas.security.filter.OlvidePasswordRateLimitFilter;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.core.env.Environment;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint cCustomAuthenticationEntryPoint;
    private final Environment env;

    @Autowired
    public SecurityConfig(CustomAccessDeniedHandler customAccessDeniedHandler,
                         CustomAuthenticationEntryPoint cCustomAuthenticationEntryPoint,
                         Environment env) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.cCustomAuthenticationEntryPoint = cCustomAuthenticationEntryPoint;
        this.env = env;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                           UserDetailsServiceImpl userDetailsService,
                                                           ITokenEmitidoRepository tokenEmitidoRepository) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, tokenEmitidoRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   OlvidePasswordRateLimitFilter rateLimitFilter) throws Exception {
        http.csrf(csrf -> csrf.disable());

        // Solo forzar HTTPS si el perfil activo NO es "test"
        boolean isTestProfile = false;
        for (String profile : env.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("test")) {
                isTestProfile = true;
                break;
            }
        }
        if (!isTestProfile) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        return http
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(cCustomAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PublicRoutes.RUTAS_PUBLICAS).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
            .build();
    }
}