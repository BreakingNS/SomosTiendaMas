package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenResetPasswordRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService {
    
    private final ITokenResetPasswordRepository tokenResetPasswordRepository;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordService(ITokenResetPasswordRepository tokenResetPasswordRepository, IUsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.tokenResetPasswordRepository = tokenResetPasswordRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public void resetearPassword(String token, String nuevaPassword) {
        Optional<TokenResetPassword> optional = tokenResetPasswordRepository.findByToken(token);

        if (optional.isEmpty() || optional.get().isExpirado() || optional.get().isUsado()) {
            throw new RuntimeException("Token inválido o expirado");
        }

        TokenResetPassword tokenEntity = optional.get();
        Usuario usuario = tokenEntity.getUsuario(); // supondremos que el token guarda un Usuario

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        tokenEntity.setUsado(true); // Marcamos el token como usado
        tokenResetPasswordRepository.save(tokenEntity);
    }
    
}
