package com.ojosama.blacklist.domain.event.payload;

import java.util.UUID;

public record UserBlacklistStatusEvent (
        UUID userId,
        String status // ACTIVE(차단) 또는 INACTIVE(해제)
){ }
