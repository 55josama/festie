package com.ojosama.common.audit;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        log.info("principal: {}", principal);

        if (principal instanceof UUID uuid) {
            return Optional.of(uuid);
        }

        if (principal instanceof String str) {
            return Optional.of(UUID.fromString(str));
        }

        return Optional.empty();
    }
}
