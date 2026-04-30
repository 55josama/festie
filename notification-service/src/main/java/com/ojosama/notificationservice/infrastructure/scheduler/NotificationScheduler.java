package com.ojosama.notificationservice.infrastructure.scheduler;

import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanNotifications() {
        LocalDateTime time = LocalDateTime.now().minusDays(15);

        Long count = notificationRepository.deleteOldNotifications(time);

        log.info("자동 삭제 된 알림 {}건, 기준 날짜 {}", count, time);
    }

}
