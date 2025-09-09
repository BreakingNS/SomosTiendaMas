package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import com.breakingns.SomosTiendaMas.auth.repository.IEmailVerificacionRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.EmailVerificationException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificacionService {

    private final IEmailVerificacionRepository emailVerificacionRepository;
    private final IUsuarioRepository usuarioRepository;

    public EmailVerificacionService(IEmailVerificacionRepository emailVerificacionRepository,
                                     IUsuarioRepository usuarioRepository) {
        this.emailVerificacionRepository = emailVerificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Genera y guarda el código de verificación para el usuario
    public EmailVerificacion generarCodigoParaUsuario(Usuario usuario) {
        String codigo = generarCodigoSeguro(32);
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1); // 1h de validez
        EmailVerificacion verificacion = new EmailVerificacion(codigo, usuario, expiracion);
        return emailVerificacionRepository.save(verificacion);
    }

    // Genera un código alfanumérico y simbólico de longitud dada
    private String generarCodigoSeguro(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Verifica el código recibido
    public boolean verificarCodigo(String codigo) {
        Optional<EmailVerificacion> opt = emailVerificacionRepository.findByCodigoAndUsadoFalse(codigo);
        if (opt.isEmpty()) {
            throw new EmailVerificationException("Código inválido o ya usado");
        }
        EmailVerificacion verificacion = opt.get();
        System.out.println("\n\n1Expiración: " + verificacion.getFechaExpiracion() + " | Ahora: " + LocalDateTime.now() + "\n\n");
        if (verificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            System.out.println("\n\n2Expiración: " + verificacion.getFechaExpiracion() + " | Ahora: " + LocalDateTime.now() + "\n\n");
            throw new EmailVerificationException("El código está expirado");
        }
        System.out.println("\n\n3Expiración: " + verificacion.getFechaExpiracion() + " | Ahora: " + LocalDateTime.now() + "\n\n");
        verificacion.setUsado(true);
        emailVerificacionRepository.save(verificacion);
        Usuario usuario = verificacion.getUsuario();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
        System.out.println("\n\n4Expiración: " + verificacion.getFechaExpiracion() + " | Ahora: " + LocalDateTime.now() + "\n\n");
        return true;
    }
}