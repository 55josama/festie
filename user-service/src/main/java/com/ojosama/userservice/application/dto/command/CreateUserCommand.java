package com.ojosama.userservice.application.dto.command;

public record CreateUserCommand(
        String email,
        String password,
        String nickname,
        String name,
        String phoneNumber
) {
}
