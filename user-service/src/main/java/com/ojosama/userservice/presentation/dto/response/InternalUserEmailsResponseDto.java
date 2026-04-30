package com.ojosama.userservice.presentation.dto.response;

import java.util.Map;
import java.util.UUID;

public record InternalUserEmailsResponseDto(
        Map<UUID, String> emails
) {
    public static InternalUserEmailsResponseDto from(Map<UUID, String> emails) {
        return new InternalUserEmailsResponseDto(emails);
    }
}