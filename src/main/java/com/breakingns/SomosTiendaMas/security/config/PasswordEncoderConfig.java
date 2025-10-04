package com.breakingns.SomosTiendaMas.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

@Configuration
@Profile("!test") // no cargar esta clase cuando el profile 'test' esté activo
public class PasswordEncoderConfig {

    // Hibrido para soportar hashes antiguos (bcrypt) y nuevos (argon2id)
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");
    private static final Pattern ARGON2_PATTERN = Pattern.compile("^\\$argon2id\\$.*");

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Encoder para nuevos hashes (Argon2id) - parámetros para MVP
        Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(
            16, // saltLength bytes
            32, // hashLength bytes
            1,  // parallelism
            65536, // memory KB = 64 MiB
            2    // iterations
        );

        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(); // ajustar strength si hace falta

        // PasswordEncoder que delega según formato del hash almacenado
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return argon2.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) return false;
                if (BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
                    boolean ok = bcrypt.matches(rawPassword, encodedPassword);
                    // Si es bcrypt y coincide, idealmente re-hashear con Argon2 en el flujo de login
                    return ok;
                }
                if (ARGON2_PATTERN.matcher(encodedPassword).matches()) {
                    return argon2.matches(rawPassword, encodedPassword);
                }
                // fallback: intentar ambos por compatibilidad
                return bcrypt.matches(rawPassword, encodedPassword) || argon2.matches(rawPassword, encodedPassword);
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                // indicar si debemos re-hashear (true si era bcrypt)
                return encodedPassword != null && BCRYPT_PATTERN.matcher(encodedPassword).matches();
            }
        };
    }

    /* 
    // Mas seguro para produccion pero mas lento
    @Bean
    public PasswordEncoder passwordEncoder() {
        // saltLength (bytes), hashLength (bytes), parallelism, memory (KB), iterations
        int saltLength = 16;      // bytes
        int hashLength = 32;      // bytes
        int parallelism = 1;      // lanes
        int memory = 65536;       // KB -> 64 MiB
        int iterations = 2;       // time cost

        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }*/
    /* 
    // Rapido para Tests
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    */
}
