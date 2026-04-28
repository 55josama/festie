package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.BlackListRegisterEventMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.consumer.dto.TargetBlindEventMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationHandler {

    private final NotificationRepository notificationRepository;

    // TODO : 자동으로 처리 된 블랙리스트 (관리자 알림)
    public void handleBlackListRegister(BlackListRegisterEventMessage message) {
        // TODO : feign으로 adminId 조회
        UUID adminId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        notificationRepository.save(Notification.of(adminId, "운영알림", message.reason() + "로 블랙리스트 추가되었습니다.",
                TargetInfo.of(message.userId(), Target.OPERATION, TargetType.BLACKLIST_REGISTERED)));
    }

    // TODO : 블라인드 처리(각각의 카테고리 관리자에게 알림)
    public void handleBlindRegister(TargetBlindEventMessage message) {
        // TODO : feign으로 managerId조회
        UUID managerId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        notificationRepository.save(Notification.of(managerId, "운영알림", message.targetType() + "에서 블라인드 처리되었습니다.",
                TargetInfo.of(message.targetId(), Target.OPERATION, TargetType.BLIND_REGISTERED)));
    }


}
