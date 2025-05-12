package com.breakingns.SomosTiendaMas.security.rate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterService {
    private final Map<String, List<Instant>> attempts = new ConcurrentHashMap<>();

    private final int MAX_ATTEMPTS = 5;
    private final Duration WINDOW = Duration.ofMinutes(1);

    public boolean isBlocked(String key) {
        List<Instant> timestamps = attempts.getOrDefault(key, new ArrayList<>());

        // Eliminar timestamps fuera de ventana de tiempo
        Instant cutoff = Instant.now().minus(WINDOW);
        timestamps.removeIf(t -> t.isBefore(cutoff));

        // Registrar nuevo intento
        timestamps.add(Instant.now());
        attempts.put(key, timestamps);

        return timestamps.size() > MAX_ATTEMPTS;
    }
}