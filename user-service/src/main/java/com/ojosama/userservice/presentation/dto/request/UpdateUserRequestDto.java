package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.UpdateUserCommand;
import java.util.UUID;

public record UpdateUserRequestDto(
        String email,
        String nickname
) {
    public UpdateUserCommand toCommand(UUID userId) {
        return new UpdateUserCommand(
                userId,
                email,
                nickname
        );
    }
}
