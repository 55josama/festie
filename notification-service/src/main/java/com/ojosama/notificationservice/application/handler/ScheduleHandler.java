package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.domain.model.emailLog.EmailLog;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.repository.EmailLogRepository;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.client.dto.UserInfo;
import com.ojosama.notificationservice.infrastructure.mail.MailService;
import com.ojosama.notificationservice.infrastructure.mail.dto.MailSendDto;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.CalendarScheduleMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.TicketingScheduleMessage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final UserClient userClient;

    // 행사 일정 임박
    public void handleEventScheduled(CalendarScheduleMessage message) {
        UserInfo userInfo = userClient.getUserInfo(message.userIds());
        Map<UUID, String> emailMap = userInfo.userInfo();

        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(
                        receiverId, message.eventName() + " 행사 알림",
                        message.eventStartAt() + "에 시작됩니다.",
                        TargetInfo.event(message.eventId())))
                .toList();
        notificationRepository.saveAll(notifications);
        notifications.forEach(notification -> {
            String email = emailMap.get(notification.getReceiverId());
            if (email == null || email.isBlank()) {
                log.warn("수신자 이메일 누락: {}", notification.getReceiverId());
                return;
            }
            send(notification, email);
        });
        log.info("행사 임박 메일 전송");
    }

    // 티켓팅 일정 임박
    public void handleTicketingScheduled(TicketingScheduleMessage message) {
        UserInfo userInfo = userClient.getUserInfo(message.userIds());
        Map<UUID, String> emailMap = userInfo.userInfo();

        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(
                        receiverId, message.eventName() + " 티켓팅 알림",
                        message.ticketingStartAt() + "에 시작됩니다.",
                        TargetInfo.ticketing(message.eventId())))
                .toList();

        notificationRepository.saveAll(notifications);
        notifications.forEach(notification -> {
            String email = emailMap.get(notification.getReceiverId());
            if (email == null || email.isBlank()) {
                log.warn("수신자 이메일 누락: {}", notification.getReceiverId());
                return;
            }
            send(notification, email);
        });
        log.info("티켓팅 메일 전송");
    }

    private void send(Notification notification, String email) {
        EmailLog emailLog = null;

        try {
            MailSendDto mailSendDto = MailSendDto.of(email, notification.getTitle(), notification.getContent());
            emailLog = emailLogRepository.save(EmailLog.of(notification, email));
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
