package com.breakingns.SomosTiendaMas.security.listener;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureListener {

    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    @Autowired
    public LoginFailureListener(LoginAttemptService loginAttemptService, HttpServletRequest request) {
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    // Ejemplo de método para escuchar eventos de login fallido
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        String ip = request.getRemoteAddr();
        loginAttemptService.loginFailed(username, ip);
    }
}