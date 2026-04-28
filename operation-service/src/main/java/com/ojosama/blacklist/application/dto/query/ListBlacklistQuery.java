package com.ojosama.blacklist.application.dto.query;

import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;

public record ListBlacklistQuery (
        BlacklistStatus blacklistStatus
){ }
