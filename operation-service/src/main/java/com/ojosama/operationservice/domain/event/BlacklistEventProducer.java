package com.ojosama.operationservice.domain.event;

import com.ojosama.operationservice.domain.event.payload.UserBlacklistStatusEvent;

public interface BlacklistEventProducer {
    // user-service에 차단된 userId임을 알리는 이벤트
    void publishStatusChangeEvent(UserBlacklistStatusEvent event);
}
