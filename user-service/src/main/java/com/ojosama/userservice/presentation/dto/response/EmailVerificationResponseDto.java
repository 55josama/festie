package com.ojosama.userservice.presentation.dto.response;

public record EmailVerificationResponseDto(
        String email,
        String message
) {
}