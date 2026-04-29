package com.ojosama.eventservice.event.domain.event;

import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventUpdatedMessage;

public interface EventMessagePublisher {
    void publishEventCreated(EventCreatedMessage message);
    void publishEventDeleted(EventDeletedMessage message);
    void publishEventUpdated(EventUpdatedMessage message);
    void publishScheduleChanged(EventScheduleChangedMessage message);
}
