package com.ojosama.operationservice.application.dto.result;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.model.enums.RegistrationType;
import java.util.UUID;

public record BlacklistResult (
        UUID id,
        UUID userId,
        BlacklistStatus status,
        String reason,
        RegistrationType registrationType
){
    public static BlacklistResult from(Blacklist blacklist) {
        return new BlacklistResult(
                blacklist.getId(),
                blacklist.getUserId(),
                blacklist.getStatus(),
                blacklist.getReason(),
                blacklist.getRegistrationType()
        );
    }
}
