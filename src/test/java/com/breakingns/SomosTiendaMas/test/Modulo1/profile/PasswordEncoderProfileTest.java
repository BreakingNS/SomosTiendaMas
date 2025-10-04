package com.breakingns.SomosTiendaMas.test.Modulo1.profile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderProfileTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void encoderIsBcryptInTestProfile() {
        String clazz = passwordEncoder.getClass().getSimpleName().toLowerCase();
        assertTrue(clazz.contains("bcrypt"), "Se esperaba BCrypt en profile test, pero fue: " + passwordEncoder.getClass());
    }
}