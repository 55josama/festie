package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import com.ojosama.eventservice.event.domain.repository.EventScheduleActionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduleActionPoller {

    private final EventScheduleActionRepository scheduleActionRepo;
    private final EventRepository eventRepository;

    @Scheduled(fixedDelayString = "${festie.event.schedule-action.poll-delay-ms:60000}")
    @Transactional
    public void poll() {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 100);

        List<EventScheduleAction> actions = scheduleActionRepo
            .findPendingByScheduledAtBefore(now, pageable);

        if (actions.isEmpty()) {
            return;
        }

        log.info("[Event Schedule] 처리 시작. count={}", actions.size());

        for (EventScheduleAction action : actions) {
            try {
                Event event = eventRepository.findById(action.getEventId())
                    .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

                executeAction(event, action);

                action.markExecuted(LocalDateTime.now());
                scheduleActionRepo.save(action);

                log.info("[Event Schedule] 실행 성공. eventId={}, action={}",
                    action.getEventId(), action.getAction());

            } catch (Exception e) {
                log.error("[Event Schedule] 실행 실패. actionId={}, error={}",
                    action.getId(), e.getMessage(), e);
                action.markFailed(e.getMessage());
                scheduleActionRepo.save(action);
            }
        }
    }

    private void executeAction(Event event, EventScheduleAction action) {
        switch (action.getAction()) {
            case MARK_IN_PROGRESS:
                event.markInProgress();
                eventRepository.save(event);
                break;

            case MARK_COMPLETED:
                event.markCompleted();
                eventRepository.save(event);
                break;
        }
    }
}
