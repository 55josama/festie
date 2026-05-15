package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.application.command.CalendarStatusChangeCommand;
import com.ojosama.notificationservice.application.command.EventDeletedCommand;
import com.ojosama.notificationservice.application.command.EventRequestCommand;
import com.ojosama.notificationservice.application.command.EventRequestResultCommand;
import com.ojosama.notificationservice.application.command.EventUpdatedCommand;
import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.sse.SseEmitterManager;
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

    private final SseEmitterManager sseEmitterManager;

    public void handleEventChanged(EventUpdatedCommand command) {
        String content = command.changedFields().stream()
                .map(f -> f.fieldName() + f.before() + " -> " + f.after() + " 로 변경되었습니다.")
                .collect(Collectors.joining("\n"));

        List<Notification> notifications = command.userIds().stream()
                .map(receiverId -> Notification.of(receiverId, command.eventName(), content,
                        TargetInfo.of(command.eventId(), Target.EVENT,
                                TargetType.EVENT_CHANGED)))
                .toList();

        notificationRepository.saveAll(notifications);

        notifications.forEach(n ->
                sseEmitterManager.broadcast(n.getReceiverId(), NotificationResult.of(n))
        );
    }

    public void handleEventCanceled(EventDeletedCommand command) {
        if (command.userIds() == null || command.userIds().isEmpty()) {
            log.info("행사 삭제 알림 대상이 비어 있습니다: {}", command.eventId());
            return;
        }
        List<Notification> notifications = command.userIds().stream()
                .map(receiverId -> Notification.of(receiverId, "행사 삭제 알림", command.eventName() + " 행사가 취소 되었습니다.",
                        TargetInfo.of(command.eventId(), Target.EVENT, TargetType.EVENT_CANCELED)))
                .toList();

        notificationRepository.saveAll(notifications);

        notifications.forEach(n ->
                sseEmitterManager.broadcast(n.getReceiverId(), NotificationResult.of(n))
        );
    }

    public void handleEventStatusChange(CalendarStatusChangeCommand command) {
        if (command.userIds() == null || command.userIds().isEmpty()) {
            log.info("행사 취소 알림 대상이 비어 있습니다: {}", command.eventId());
            return;
        }
        List<Notification> notifications = command.userIds().stream()
                .map(receiverId -> Notification.of(receiverId, "행사 취소 알림", command.eventName() + " 행사가 취소 되었습니다.",
                        TargetInfo.of(command.eventId(), Target.EVENT, TargetType.EVENT_CANCELED)))
                .toList();

        notificationRepository.saveAll(notifications);

        notifications.forEach(n ->
                sseEmitterManager.broadcast(n.getReceiverId(), NotificationResult.of(n))
        );
    }


    public void handleEventRequest(EventRequestCommand command) {
        UUID managerId = userClient.getManagerInfo(command.categoryName());

        Notification notification = notificationRepository.save(
                Notification.of(managerId, "행사 요청", "승인을 기다리는 요청이 있습니다.",
                        TargetInfo.of(command.targetId(), Target.EVENT, TargetType.EVENT_REQUEST)));

        sseEmitterManager.broadcast(managerId, NotificationResult.of(notification));
    }

    public void handleEventRequestResult(EventRequestResultCommand command) {
        String content = "요청하신 " + command.eventName() + " 결과가 " + command.status() + "되었습니다.";

        Notification notification = notificationRepository.save(
                Notification.of(command.receiverId(), "행사 요청 결과", content,
                        TargetInfo.of(command.targetId(), Target.EVENT, TargetType.EVENT_REQUEST_RESULT)));

        sseEmitterManager.broadcast(command.receiverId(),
                NotificationResult.of(notification));
    }


}
