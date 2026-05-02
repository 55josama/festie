package com.ojosama.common.audit;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000000")); //테스트를 위한 임의값 지정
    }
}
