package com.ojosama.operationservice.domain.event;

import com.ojosama.operationservice.domain.event.payload.BlacklistRegisterEvent;
import com.ojosama.operationservice.domain.event.payload.UserBlacklistStatusEvent;

public interface BlacklistEventProducer {
    // user-service에 차단된 userId임을 알리는 이벤트
    void publishStatusChangeEvent(UserBlacklistStatusEvent event);

    // 수동 등록 시 notification-service에 알리기 위한 메서드 추가
    void publishBlacklistRegisterEvent(BlacklistRegisterEvent event);
}
