package com.ojosama.notificationservice.application;

import com.ojosama.notificationservice.application.handler.EventHandler;
import com.ojosama.notificationservice.application.handler.OperationHandler;
import com.ojosama.notificationservice.application.handler.ScheduleHandler;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.BlackListRegisterEventMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.BlackListSendEmailMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.CalendarScheduleMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventDeletedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventRequestCreatedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventRequestCreatedResultMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.OperationRequestMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.TargetBlindEventMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.TicketingScheduleMessage;
import com.ojosama.notificationservice.presentation.dto.NotificationResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final EventHandler eventHandler;
    private final ScheduleHandler scheduleHandler;
    private final OperationHandler operationHandler;

    public void createNotificationTicketing(TicketingScheduleMessage message) {
        scheduleHandler.handleTicketingScheduled(message);
    }

    public void createNotificationEvent(CalendarScheduleMessage message) {
        scheduleHandler.handleEventScheduled(message);
    }

    public void updateEventNotification(EventUpdatedMessage message) {
        eventHandler.handleEventChanged(message);
    }

    public void deleteEventNotification(EventDeletedMessage message) {
        eventHandler.handleEventCanceled(message);
    }

    public void createEventRequestNotification(EventRequestCreatedMessage message) {
        eventHandler.handleEventRequest(message);
    }

    public void createEventRequestResultNotification(EventRequestCreatedResultMessage message) {
        eventHandler.handleEventRequestResult(message);
    }

    public void createBlacklistRequestNotification(BlackListRegisterEventMessage message) {
        operationHandler.handleBlackListRequest(message);
    }

    public void blackListSendEmail(BlackListSendEmailMessage message) {
        operationHandler.handelSendBlackListEmail(message);
    }

    public void operationRequest(OperationRequestMessage message) {
        operationHandler.handleOperationRequest(message);
    }

    public void blindNotification(TargetBlindEventMessage message) {
        operationHandler.handleBlindRegister(message);
    }

    public List<NotificationResponse> markAllAsRead(UUID receiverId) {

        List<Notification> list = notificationRepository.findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(receiverId);

        for (Notification notification : list) {
            notification.readAt();
        }

        return list.stream()
                .map(NotificationResponse::of)
                .toList();
    }

    public void deleteNotification(UUID receiverId, UUID notificationId) {

        Notification notification = notificationRepository.findByIdAndReceiverIdAndDeletedAtIsNull(notificationId,
                        receiverId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOT_FOUND_NOTIFICATION));

        notification.deleted();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> selectAll(UUID receiverId) {

        List<Notification> list = notificationRepository.findByReceiverIdAndDeletedAtIsNull(receiverId);

        return list.stream()
                .map(NotificationResponse::of)
                .toList();
    }

}
