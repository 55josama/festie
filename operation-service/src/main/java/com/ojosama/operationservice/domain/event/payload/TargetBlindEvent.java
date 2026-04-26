package com.ojosama.operationservice.domain.event.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TargetBlindEvent {
    private UUID targetId;
    private String targetType;
    private String notifyTargetRole; // 알림 받을 대상
    private String message; // 알림 메세지 내용
}
