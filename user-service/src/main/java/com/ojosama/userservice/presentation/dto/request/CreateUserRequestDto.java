package com.ojosama.userservice.presentation.dto.request;

import com.ojosama.userservice.application.dto.command.CreateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDto(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "휴대전화 번호는 필수입니다.")
        String phoneNumber
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(email, password, nickname, name, phoneNumber);
    }
}
