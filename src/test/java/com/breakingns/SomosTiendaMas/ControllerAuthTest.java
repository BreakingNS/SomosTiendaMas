package com.breakingns.SomosTiendaMas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.helpers.TokenHelper;
import com.breakingns.SomosTiendaMas.auth.utils.CookieUtils;

@RestController
@RequestMapping("/test/api/auth")
public class ControllerAuthTest {
    
    private final RefreshTokenServiceTest refreshTokenServiceTest;

    public ControllerAuthTest(RefreshTokenServiceTest refreshTokenServiceTest) {
        this.refreshTokenServiceTest = refreshTokenServiceTest;
    }


    @PostMapping("/public/refresh-token")
    public ResponseEntity<Map<String, String>> refrescarToken(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request, 
            HttpServletResponse response) {

        String refreshToken = TokenHelper.extractRefreshToken(request, body);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token no encontrado"));
        }

        AuthResponse tokens = refreshTokenServiceTest.refrescarTokens(refreshToken, request);
        CookieUtils.setAuthCookies(response, tokens.getAccessToken(), tokens.getRefreshToken(), false);

        return ResponseEntity.ok(Map.of(
            "message", "Tokens renovados exitosamente"
        ));
    }

}
