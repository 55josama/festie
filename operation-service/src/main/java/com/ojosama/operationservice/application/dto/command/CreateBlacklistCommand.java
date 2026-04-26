package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CreateBlacklistCommand {
    private final UUID userId;
    private final String reason;

    public CreateBlacklistCommand(UUID userId, String reason) {
        if (userId == null) {
            throw new IllegalArgumentException("블랙리스트 등록 대상 유저 ID는 필수입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("블랙리스트 등록 사유는 필수입니다.");
        }

        this.userId = userId;
        this.reason = reason;
    }

    public Blacklist toEntity(){
        return Blacklist.of(userId, reason);
    }
}
