package com.ojosama.operationservice.application.dto.result;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import java.util.UUID;

public record BlacklistResult (
        UUID id,
        UUID userId,
        String status,
        String reason
){
    public static BlacklistResult from(Blacklist blacklist) {
        return new BlacklistResult(
                blacklist.getId(),
                blacklist.getUserId(),
                blacklist.getStatus().name(),
                blacklist.getReason()
        );
    }
}
