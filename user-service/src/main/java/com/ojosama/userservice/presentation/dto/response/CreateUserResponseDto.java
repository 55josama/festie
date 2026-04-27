package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.domain.model.UserRole;
import java.util.UUID;

public record CreateUserResponseDto(
        UUID userId,
        String email,
        String nickname,
        String name,
        UserRole role
) {

    public static CreateUserResponseDto from(CreateUserResult result) {
        return new CreateUserResponseDto(
                result.userId(),
                result.email(),
                result.nickname(),
                result.name(),
                result.role()
        );
    }
}
