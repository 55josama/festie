package com.ojosama.userservice.presentation.dto.response;

import java.util.Map;
import java.util.UUID;

public record InternalUserEmailsResponseDto(
        Map<UUID, String> userInfo
) {
    public static InternalUserEmailsResponseDto from(Map<UUID, String> userInfo) {
        return new InternalUserEmailsResponseDto(userInfo);
    }
}
