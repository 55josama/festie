package com.ojosama.operationservice.domain.event.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserBlacklistStatusEvent {
    private UUID userId;
    private String status; // ACTIVE(차단) 또는 INACTIVE(해제)
}
