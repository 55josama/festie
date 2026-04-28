package com.ojosama.blacklist.presentation.dto;

import com.ojosama.blacklist.application.dto.command.UpdateBlacklistCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateBlacklistRequest (
        @NotBlank(message = "블랙리스트 해제 사유를 입력해주세요.")
        String reason
){
    public UpdateBlacklistCommand toCommand() {
        return new UpdateBlacklistCommand(reason);
    }
}
