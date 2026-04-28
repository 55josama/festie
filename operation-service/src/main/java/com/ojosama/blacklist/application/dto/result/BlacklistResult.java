package com.ojosama.blacklist.application.dto.result;

import com.ojosama.blacklist.domain.model.entity.Blacklist;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import com.ojosama.blacklist.domain.model.enums.RegistrationType;
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
