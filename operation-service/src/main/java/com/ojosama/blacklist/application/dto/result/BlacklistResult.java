package com.ojosama.blacklist.application.dto.result;

import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record BlacklistResult (
        UUID id,
        UUID userId,
        BlacklistStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static BlacklistResult from(Blacklist blacklist) {
        return new BlacklistResult(
                blacklist.getId(),
                blacklist.getUserId(),
                blacklist.getStatus(),
                blacklist.getReason(),
                blacklist.getCreatedAt(),
                blacklist.getUpdatedAt()
        );
    }
}
