package com.ojosama.operationservice.application.dto.query;

import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;

public record ListBlacklistQuery (
        BlacklistStatus blacklistStatus
){ }
