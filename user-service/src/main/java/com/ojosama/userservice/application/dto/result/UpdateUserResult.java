package com.ojosama.userservice.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateUserResult(
        UUID userId,
        String email,
        String nickname,
        LocalDateTime updatedAt
) {
}
