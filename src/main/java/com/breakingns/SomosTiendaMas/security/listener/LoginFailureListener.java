package com.breakingns.SomosTiendaMas.security.listener;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    
}