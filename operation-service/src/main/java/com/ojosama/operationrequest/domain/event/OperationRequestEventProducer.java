package com.ojosama.operationrequest.domain.event;

import com.ojosama.operationrequest.domain.event.payload.OperationRequestCreateEvent;

public interface OperationRequestEventProducer {
    void publishOperationRequestCreateEvent(OperationRequestCreateEvent event);
}
