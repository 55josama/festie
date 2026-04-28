package com.ojosama.userservice.application.dto.result;

import java.util.UUID;

public record LogoutResult(
        UUID userId
) {
}