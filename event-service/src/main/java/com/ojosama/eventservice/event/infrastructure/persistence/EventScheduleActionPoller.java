package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.repository.EventScheduleActionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventScheduleActionPoller {

    private final EventScheduleActionRepository scheduleActionRepo;
    private final EventScheduleActionExecutor executor;

    @Scheduled(fixedDelayString = "${festie.event.schedule-action.poll-delay-ms:60000}")
    @SchedulerLock(
            name = "EventScheduleActionPoller",
            lockAtMostFor = "PT55S",
            lockAtLeastFor = "PT5S"
    )
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
            executor.processOne(action);
        }
    }
}
