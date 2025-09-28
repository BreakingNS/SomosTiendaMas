package com.breakingns.SomosTiendaMas.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Set<String> EMAILS_BLOQUEADOS = Set.of(
        "correoprueba@noenviar.com",
        "correoprueba1@noenviar.com",
        "correoprueba2@noenviar.com",
        "correoempresa@noenviar.com",
        "correoempresa1@noenviar.com",
        "correoempresa2@noenviar.com"
    );


    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailVerificacion(String destinatario, String token) {
        if (EMAILS_BLOQUEADOS.contains(destinatario)) {
            System.out.println("\n\nNO SE ENVIA CORREO!!!!\n\n");
            return;
        }
        try {
            System.out.println("\n\nSI SE ENVIA CORREO!!!!\n\n");
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
            helper.setTo(destinatario);
            helper.setSubject("Verifica tu email");

            String enlace = "https://localhost:8443/public/correoVerificado.html?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            String html = "<div style='font-family: Arial, sans-serif; color: #333; background: #f9f9f9; padding: 24px; border-radius: 8px;'>"
                + "<h2 style='color: #1976d2;'>¡Bienvenido a SomosTiendaMas!</h2>"
                + "<p>Gracias por registrarte. Para activar tu cuenta, haz clic en el siguiente enlace:</p>"
                + "<a href='" + enlace + "' style='display:inline-block; background:#1976d2; color:#fff; padding:10px 24px; border-radius:6px; text-decoration:none; font-weight:bold; margin:16px 0;'>Verificar email</a>"
                + "<p style='margin-top:16px;'>O copia y pega este enlace en tu navegador:</p>"
                + "<div style='font-size: 1em; color: #388e3c; font-weight: bold; margin: 8px 0;'>" + enlace + "</div>"
                + "<span class='text-orange-500'>Tienda</span>"
                + "<span class='text-orange-500'>Tienda</span>"
                + "<p style='font-size: 0.9em; color: #888;'>Si no solicitaste este registro, ignora este mensaje.</p>"
                + "<hr style='margin:24px 0 8px 0; border:none; border-top:1px solid #e0e0e0;'>"
                + "<footer style='font-size:0.85em; color:#aaa; text-align:center;'>© SomosTiendaMas 2025. Todos los derechos reservados.</footer>"
                + "</div>";

            helper.setText(html, true); // true = HTML
            mailSender.send(mensaje);
            System.out.println("\n\nSI SE ENVIA CORREO!!!!\n\n");
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email de verificación", e);
        }
    }

    public void enviarEmailRecuperacionPassword(String destinatario, String token) {
        if (EMAILS_BLOQUEADOS.contains(destinatario)) {
            System.out.println("\n\nNO SE ENVIA CORREO!!!!\n\n");
            return;
        }
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
            helper.setTo(destinatario);
            helper.setSubject("Restablece tu contraseña");

            String enlace = "https://localhost:8443/public/cambiarContrasenia.html?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            String html = "<div style='font-family: Arial, sans-serif; color: #333; background: #f9f9f9; padding: 24px; border-radius: 8px;'>"
                + "<h2 style='color: #d32f2f;'>Recuperación de contraseña</h2>"
                + "<p>Solicitaste recuperar tu contraseña. Para restablecerla, haz clic en el siguiente enlace:</p>"
                + "<a href='" + enlace + "' style='display:inline-block; background:#1976d2; color:#fff; padding:10px 24px; border-radius:6px; text-decoration:none; font-weight:bold; margin:16px 0;'>Restablecer contraseña</a>"
                + "<p style='margin-top:16px;'>O copia y pega este enlace en tu navegador:</p>"
                + "<div style='font-size: 1em; color: #388e3c; font-weight: bold; margin: 8px 0;'>" + enlace + "</div>"
                + "<p style='font-size: 0.9em; color: #888;'>Si no solicitaste esta recuperación, ignora este mensaje.</p>"
                + "<hr style='margin:24px 0 8px 0; border:none; border-top:1px solid #e0e0e0;'>"
                + "<footer style='font-size:0.85em; color:#aaa; text-align:center;'>© SomosTiendaMas 2025. Todos los derechos reservados.</footer>"
                + "</div>";

            helper.setText(html, true);
            mailSender.send(mensaje);
            System.out.println("\n\nSI SE ENVIA CORREO DE RECUPERACION!!!!\n\n");
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email de recuperación de contraseña", e);
        }
    }
}