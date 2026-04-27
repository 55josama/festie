package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import com.ojosama.operationservice.domain.model.entity.Blacklist;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.model.enums.RegistrationType;
import java.util.UUID;

public record ListBlacklistResponse (
        UUID id,
        UUID userId,
        BlacklistStatus status,
        RegistrationType registrationType
){
    public static ListBlacklistResponse from(Blacklist blacklist) {
        return new ListBlacklistResponse(
                blacklist.getId(),
                blacklist.getUserId(),
                blacklist.getStatus(),
                blacklist.getRegistrationType()
        );
    }
}
