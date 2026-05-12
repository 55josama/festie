package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import com.ojosama.eventservice.event.domain.repository.EventScheduleActionRepository;
import com.ojosama.eventservice.event.domain.event.payload.EventStatusChangedMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduleActionExecutor {

    private final EventScheduleActionRepository scheduleActionRepo;
    private final EventRepository eventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(EventScheduleAction action) {
        try {
            Event event = eventRepository.findByIdForUpdate(action.getEventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

            executeAction(event, action);
            action.markExecuted(LocalDateTime.now());

            log.info("[Event Schedule] 실행 성공. eventId={}, action={}",
                action.getEventId(), action.getAction());

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[Event Schedule] 실행 실패. actionId={}, error={}",
                action.getId(), errorMessage, e);
            action.markFailed(errorMessage);
        } finally {
            scheduleActionRepo.save(action);
        }
    }

    private void executeAction(Event event, EventScheduleAction action) {
        String beforeStatus = event.getStatus().name();

        switch (action.getAction()) {
            case MARK_IN_PROGRESS:
                event.markInProgress();
                eventRepository.save(event);
                applicationEventPublisher.publishEvent(new EventStatusChangedMessage(
                    event.getId(),
                    event.getName(),
                    beforeStatus,
                    event.getStatus().name(),
                    java.util.Collections.emptyList()
                ));
                break;

            case MARK_COMPLETED:
                event.markCompleted();
                eventRepository.save(event);
                applicationEventPublisher.publishEvent(new EventStatusChangedMessage(
                    event.getId(),
                    event.getName(),
                    beforeStatus,
                    event.getStatus().name(),
                    java.util.Collections.emptyList()
                ));
                break;
        }
    }
}
