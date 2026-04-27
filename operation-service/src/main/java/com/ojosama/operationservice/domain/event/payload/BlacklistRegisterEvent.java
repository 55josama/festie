package com.ojosama.operationservice.domain.event.payload;

import com.ojosama.operationservice.domain.model.enums.RegistrationType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record BlacklistRegisterEvent (
        UUID userId, // 문제를 일으킨 유저 ID
        String reason,
        RegistrationType registrationType
){ }
