package com.ojosama.operationservice.application.dto.query;

import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListBlacklistQuery {
    private BlacklistStatus blacklistStatus;
}
