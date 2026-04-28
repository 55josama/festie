package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.domain.model.emailLog.EmailLog;
import com.ojosama.notificationservice.domain.model.emailLog.Status;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.repository.EmailLogRepository;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.mail.MailService;
import com.ojosama.notificationservice.infrastructure.mail.dto.MailSendDto;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.EventScheduleMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.TicketingScheduleMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleHandler {

    private final NotificationRepository notificationRepository;
    private final EmailLogRepository emailLogRepository;

    private final MailService mailService;

    // TODO : 행사 알림
    public void handleEventScheduled(EventScheduleMessage message) {
        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(
                        receiverId, message.eventName() + "의 행사 알림",
                        message.eventStartAt() + "에 시작됩니다.",
                        TargetInfo.event(message.eventId())))
                .toList();

        notificationRepository.saveAll(notifications);
        notifications.forEach(this::send);
        log.info("행사 임박 메일 전송");
    }

    // TODO : 티켓팅 알림
    public void handleTicketingScheduled(TicketingScheduleMessage message) {
        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(
                        receiverId, message.eventName() + "의 티켓팅 알림",
                        message.ticketingStartAt() + "에 시작됩니다.",
                        TargetInfo.ticketing(message.eventId())))
                .toList();

        notificationRepository.saveAll(notifications);
        notifications.forEach(this::send);
        log.info("티켓팅 메일 전송");

    }

    private void send(Notification notification) {
        EmailLog emailLog = null;

        try {
            // TODO : feign으로 user email 조회
            MailSendDto mailSendDto = MailSendDto.of("", notification.getTitle(), notification.getContent());

            emailLog = emailLogRepository.save(EmailLog.of(notification, "", Status.PENDING));
            mailService.sendEmail(mailSendDto);
            emailLog.successStatus();
            emailLogRepository.save(emailLog);
        } catch (NotificationException e) {
            if (emailLog != null) {
                emailLog.failStatus();
                emailLogRepository.save(emailLog);
            }
            log.error("이메일 전송 실패 : {}", notification.getReceiverId());

        }
    }
}
