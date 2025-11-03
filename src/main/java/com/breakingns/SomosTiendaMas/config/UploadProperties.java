package com.breakingns.SomosTiendaMas.config;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadProperties {
    private final Path root;
    private final String urlBase = "/uploads";

    public UploadProperties() {
        String env = System.getenv("UPLOAD_DIR"); // opcional, sobreescribe por entorno
        String base = (env != null && !env.isBlank())
                ? env
                : System.getProperty("user.home") + "/somostiendamas/uploads";
        this.root = Paths.get(base).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    public String getUrlBase() {
        return urlBase;
    }
}
