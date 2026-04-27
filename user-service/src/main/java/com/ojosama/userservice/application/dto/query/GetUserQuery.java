package com.ojosama.userservice.application.dto.query;

import java.util.UUID;

public record GetUserQuery(
        UUID userId
) {
}
