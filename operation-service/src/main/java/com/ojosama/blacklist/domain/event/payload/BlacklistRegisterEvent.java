package com.ojosama.blacklist.domain.event.payload;

import com.ojosama.blacklist.domain.model.enums.RegistrationType;
import java.util.UUID;

public record BlacklistRegisterEvent (
        UUID userId, // 문제를 일으킨 유저 ID
        String reason,
        RegistrationType registrationType
){ }
