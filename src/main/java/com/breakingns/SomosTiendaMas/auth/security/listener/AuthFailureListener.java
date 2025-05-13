package com.breakingns.SomosTiendaMas.auth.security.listener;

import com.breakingns.SomosTiendaMas.auth.service.LoginAttemptService;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFailureListener {

    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        String ip = RequestUtil.obtenerIpCliente(request);

        log.warn("Login fallido para usuario [{}] desde IP [{}]", username, ip);
        loginAttemptService.loginFailed(username, ip);
    }
}