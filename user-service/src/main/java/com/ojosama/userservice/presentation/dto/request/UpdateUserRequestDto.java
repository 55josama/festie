package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.UpdateUserCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateUserRequestDto(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phoneNumber
) {
    public UpdateUserCommand toCommand(UUID userId) {
        return new UpdateUserCommand(
                userId,
                name,
                nickname,
                phoneNumber
        );
    }
}