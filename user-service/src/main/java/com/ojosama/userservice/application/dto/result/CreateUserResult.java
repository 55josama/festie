package com.ojosama.userservice.application.dto.result;

import com.ojosama.userservice.domain.model.UserRole;
import java.util.UUID;

public record CreateUserResult(
        UUID userId,
        String email,
        String nickname,
        String name,
        UserRole role
) {
}
