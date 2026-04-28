package com.ojosama.blacklist.presentation.dto;

import com.ojosama.blacklist.application.dto.command.CreateBlacklistCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBlacklistRequest (
        @NotNull(message = "차단 대상 유저 ID를 입력해주세요.")
        UUID userId,

        @NotBlank(message = "블랙리스트 등록 사유를 입력해주세요.")
        String reason

){
    public CreateBlacklistCommand toCommand() {
        return new CreateBlacklistCommand(userId, reason);
    }
}
