package com.ojosama.notificationservice.application;

import com.ojosama.notificationservice.application.command.BlackListRegisterCommand;
import com.ojosama.notificationservice.application.command.BlackListRequestCommand;
import com.ojosama.notificationservice.application.command.CalendarScheduleCommand;
import com.ojosama.notificationservice.application.command.CalendarStatusChangeCommand;
import com.ojosama.notificationservice.application.command.EventDeletedCommand;
import com.ojosama.notificationservice.application.command.EventRequestCommand;
import com.ojosama.notificationservice.application.command.EventRequestResultCommand;
import com.ojosama.notificationservice.application.command.EventUpdatedCommand;
import com.ojosama.notificationservice.application.command.OperationRequestCommand;
import com.ojosama.notificationservice.application.command.TargetBlindEventCommand;
import com.ojosama.notificationservice.application.command.TicketingScheduleCommand;
import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import com.ojosama.notificationservice.application.handler.EventHandler;
import com.ojosama.notificationservice.application.handler.OperationHandler;
import com.ojosama.notificationservice.application.handler.ScheduleHandler;
import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public void createNotificationTicketing(TicketingScheduleCommand command) {
        scheduleHandler.handleTicketingScheduled(command);
    }

    public void createNotificationEvent(CalendarScheduleCommand command) {
        scheduleHandler.handleEventScheduled(command);
    }

    public void updateEventNotification(EventUpdatedCommand command) {
        eventHandler.handleEventChanged(command);
    }

    public void deleteEventNotification(EventDeletedCommand command) {
        eventHandler.handleEventCanceled(command);
    }

    public void createEventRequestNotification(EventRequestCommand command) {
        eventHandler.handleEventRequest(command);
    }

    public void createEventRequestResultNotification(EventRequestResultCommand command) {
        eventHandler.handleEventRequestResult(command);
    }

    public void createBlacklistRequestNotification(BlackListRequestCommand command) {
        operationHandler.handleBlackListRequest(command);
    }

    public void blackListRegister(BlackListRegisterCommand command) {
        operationHandler.handleBlackListRegister(command);
    }

    public void operationRequest(OperationRequestCommand command) {
        operationHandler.handleOperationRequest(command);
    }

    public void blindNotification(TargetBlindEventCommand command) {
        operationHandler.handleBlindRegister(command);
    }

    public void updateEventStatusNotification(CalendarStatusChangeCommand command) {
        eventHandler.handleEventStatusChange(command);
    }

    public List<NotificationResult> markAllAsRead(UUID receiverId) {

        List<Notification> list = notificationRepository.findByReceiverIdAndReadAtIsNullAndDeletedAtIsNull(receiverId);

        for (Notification notification : list) {
            notification.readAt();
        }

        return list.stream()
                .map(NotificationResult::of)
                .toList();
    }

    public void deleteNotification(UUID receiverId, UUID notificationId) {

        Notification notification = notificationRepository.findByIdAndReceiverIdAndDeletedAtIsNull(notificationId,
                        receiverId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOT_FOUND_NOTIFICATION));

        notification.deleted();
    }


    @Transactional(readOnly = true)
    public Page<NotificationResult> selectAll(UUID receiverId, Pageable pageable) {

        Page<Notification> list = notificationRepository.findByReceiverIdAndDeletedAtIsNull(receiverId, pageable);

        return list.map(NotificationResult::of);
    }


}
