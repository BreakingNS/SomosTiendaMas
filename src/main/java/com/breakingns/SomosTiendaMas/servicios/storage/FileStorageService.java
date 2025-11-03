package com.breakingns.SomosTiendaMas.servicios.storage;

import com.breakingns.SomosTiendaMas.config.UploadProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    private final UploadProperties props;

    public FileStorageService(UploadProperties props) {
        this.props = props;
        init();
    }

    private void init() {
        try {
            Files.createDirectories(props.getRoot());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar carpeta de uploads", e);
        }
    }

    public String saveImage(MultipartFile file, String subfolder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }
        String ct = file.getContentType();
        if (ct == null || ALLOWED.stream().noneMatch(ct::equalsIgnoreCase)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + ct);
        }

        Path dir = props.getRoot().resolve(normalizeFolder(subfolder));
        Files.createDirectories(dir);

        String ext = guessExtension(file);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String name = timestamp + "_" + UUID.randomUUID() + ext.toLowerCase(Locale.ROOT);

        Path target = dir.resolve(name).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // URL pública
        String relative = dir.getFileName() != null
                ? dir.getFileName().toString() + "/" + name
                : name;

        // Construir correctamente el path relativo desde la raíz de uploads
        Path relPath = props.getRoot().relativize(target).normalize();
        return props.getUrlBase() + "/" + relPath.toString().replace("\\", "/");
    }

    public void deleteByUrl(String url) {
        if (url == null || !url.startsWith(props.getUrlBase())) return;
        String rel = url.substring(props.getUrlBase().length());
        if (rel.startsWith("/")) rel = rel.substring(1);
        Path path = props.getRoot().resolve(rel).normalize();
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

    private String guessExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.'));
            // asegure extensión esperada
            return ext.matches("\\.[A-Za-z0-9]{1,6}") ? ext : mimeToExt(file.getContentType());
        }
        return mimeToExt(file.getContentType());
    }

    private String mimeToExt(String ct) {
        if (ct == null) return ".bin";
        return switch (ct.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".img";
        };
    }

    private String normalizeFolder(String f) {
        if (f == null) return "";
        return f.replace("..", "").replace("\\", "/");
    }
}
