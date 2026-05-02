package com.ojosama.eventservice.event.domain.event;

import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
public interface EventMessagePublisher {
    void publishEventCreated(EventCreatedMessage message);
    void publishEventDeleted(EventDeletedMessage message);
    void publishScheduleChanged(EventScheduleChangedMessage message);
}
