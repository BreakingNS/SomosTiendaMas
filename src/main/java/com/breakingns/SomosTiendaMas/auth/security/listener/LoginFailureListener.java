package com.breakingns.SomosTiendaMas.auth.security.listener;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
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
    
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        String ip = RequestUtil.obtenerIpCliente(request);

        loginAttemptService.loginFailed(username, ip);
    }

    
}