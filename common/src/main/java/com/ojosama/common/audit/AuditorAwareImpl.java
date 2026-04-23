package com.ojosama.common.audit;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.empty(); //todo: 사용자 값 나중ㅇ 연결 예정
    }
}