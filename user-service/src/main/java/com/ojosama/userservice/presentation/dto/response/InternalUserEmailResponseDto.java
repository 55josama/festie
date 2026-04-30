package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.InternalUserEmailResult;
import java.util.UUID;

public record InternalUserEmailResponseDto(
        UUID userId,
        String email
) {
    public static InternalUserEmailResponseDto from(InternalUserEmailResult result) {
        return new InternalUserEmailResponseDto(
                result.userId(),
                result.email()
        );
    }
}