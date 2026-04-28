package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.EventChangedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.EventDeletedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.EventRequestCreatedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.EventRequestCreatedResultMessage;
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

    // TODO : 행사 변경
    public void handleEventChanged(EventChangedMessage message) {
        // TODO : feign으로 user 리스트 조회
        List<UUID> userIds = List.of();

        String content = message.changedFields().stream()
                .map(f -> f.fieldName() + f.before() + " -> " + f.after() + " 로 변경되었습니다.")
                .collect(Collectors.joining("\n"));

        List<Notification> notifications = userIds.stream()
                .map(receiverId -> Notification.of(receiverId, message.eventName(), content,
                        TargetInfo.of(message.eventId(), Target.EVENT,
                                TargetType.EVENT_CHANGED)))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    // TODO : 행사 삭제
    public void handleEventCanceled(EventDeletedMessage message) {
        // TODO : feign으로 유저리스트 조회
        List<UUID> userIds = List.of();

        List<Notification> notifications = userIds.stream()
                .map(receiverId -> Notification.of(receiverId, "행사 취소 알림", message.eventName() + " 행사가 취소 되었습니다.",
                        TargetInfo.of(message.eventId(), Target.EVENT,
                                TargetType.EVENT_CANCELED)))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    // TODO : 행사 요청
    public void handleEventRequest(EventRequestCreatedMessage message) {
        // TODO : 안에 카테고리 이름으로 관리자 정보 가져오기
        UUID managerId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        notificationRepository.save(Notification.of(managerId, "행사 요청", "승인을 기다리는 요청이 있습니다.",
                TargetInfo.of(message.targetId(), Target.EVENT, TargetType.EVENT_REQUEST)));
    }

    // TODO : 행사 요청 결과
    public void handleEventRequestResult(EventRequestCreatedResultMessage message) {
        String content = "요청하신" + message.eventName() + " 결과가 " + message.status() + "되었습니다.";

        notificationRepository.save(Notification.of(message.receiverId(), "행사 요청 결과", content,
                TargetInfo.of(message.targetId(), Target.EVENT, TargetType.EVENT_REQUEST_RESULT)));
    }

}
