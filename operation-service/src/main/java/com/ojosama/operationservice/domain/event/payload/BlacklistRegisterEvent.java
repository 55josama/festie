package com.ojosama.operationservice.domain.event.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistRegisterEvent {
    private UUID targetUserId; // 문제를 일으킨 유저 ID
    private int blindCount; // 누적 블라인드 횟수
    private String message; // 알림 메세지 내용
}
