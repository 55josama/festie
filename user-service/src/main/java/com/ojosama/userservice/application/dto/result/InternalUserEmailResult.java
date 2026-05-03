package com.ojosama.userservice.application.dto.result;

import java.util.UUID;

public record InternalUserEmailResult(
        UUID userId,
        String email
) {
}