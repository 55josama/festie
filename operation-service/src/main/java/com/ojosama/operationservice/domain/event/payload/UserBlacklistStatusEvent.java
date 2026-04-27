package com.ojosama.operationservice.domain.event.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record UserBlacklistStatusEvent (
        UUID userId,
        String status // ACTIVE(차단) 또는 INACTIVE(해제)
){ }
