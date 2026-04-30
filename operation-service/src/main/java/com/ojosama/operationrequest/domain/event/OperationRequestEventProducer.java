package com.ojosama.operationrequest.domain.event;

import com.ojosama.operationrequest.domain.event.payload.OperationRequestCreatedEvent;

public interface OperationRequestEventProducer {
    void publishOperationRequestCreateEvent(OperationRequestCreatedEvent event);
}
