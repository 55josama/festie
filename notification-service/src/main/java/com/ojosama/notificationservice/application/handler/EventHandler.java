package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventDeletedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventRequestCreatedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventRequestCreatedResultMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    public void handleEventChanged(EventUpdatedMessage message) {
        String content = message.changedFields().stream()
                .map(f -> f.fieldName() + f.before() + " -> " + f.after() + " 로 변경되었습니다.")
                .collect(Collectors.joining("\n"));

        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(receiverId, message.eventName(), content,
                        TargetInfo.of(message.eventId(), Target.EVENT,
                                TargetType.EVENT_CHANGED)))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public void handleEventCanceled(EventDeletedMessage message) {
        if (message.userIds() == null || message.userIds().isEmpty()) {
            log.warn("행사 취소 알림 대상이 비어 있습니다: {}", message.eventId());
            return;
        }
        List<Notification> notifications = message.userIds().stream()
                .map(receiverId -> Notification.of(receiverId, "행사 취소 알림", message.eventName() + " 행사가 취소 되었습니다.",
                        TargetInfo.of(message.eventId(), Target.EVENT, TargetType.EVENT_CANCELED)))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public void handleEventRequest(EventRequestCreatedMessage message) {
        UUID managerId = userClient.getManagerInfo(message.categoryName());

        notificationRepository.save(Notification.of(managerId, "행사 요청", "승인을 기다리는 요청이 있습니다.",
                TargetInfo.of(message.targetId(), Target.EVENT, TargetType.EVENT_REQUEST)));
    }

    public void handleEventRequestResult(EventRequestCreatedResultMessage message) {
        String content = "요청하신" + message.eventName() + " 결과가 " + message.status() + "되었습니다.";

        notificationRepository.save(Notification.of(message.receiverId(), "행사 요청 결과", content,
                TargetInfo.of(message.targetId(), Target.EVENT, TargetType.EVENT_REQUEST_RESULT)));
    }

}
