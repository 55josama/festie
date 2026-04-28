package com.ojosama.blacklist.application.dto.command;

import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.RegistrationType;
import java.util.UUID;

public record CreateBlacklistCommand(UUID userId, String reason) {
    public CreateBlacklistCommand {
        if (userId == null) {
            throw new IllegalArgumentException("블랙리스트 등록 대상 유저 ID는 필수입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("블랙리스트 등록 사유는 필수입니다.");
        }
    }

    public Blacklist toEntity(RegistrationType registrationType){
        if (registrationType == null) {
            throw new IllegalArgumentException("블랙리스트 등록 유형은 필수입니다.");
        }

        return Blacklist.of(userId, reason, registrationType);
    }
}
