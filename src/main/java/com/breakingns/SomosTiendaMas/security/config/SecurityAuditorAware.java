package com.breakingns.SomosTiendaMas.security.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import java.util.Optional;
// TODO: revisar comentarios
public class SecurityAuditorAware implements AuditorAware<Long> {

    private static final Long SYSTEM_ID = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.of(SYSTEM_ID);
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof UserAuthDetails) {
            Long id = ((UserAuthDetails) principal).getId();
            return Optional.ofNullable(id != null ? id : SYSTEM_ID);
        }

        return Optional.of(SYSTEM_ID);
    }
}