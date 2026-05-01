package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.EmailLogRepository;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.mail.MailService;
import com.ojosama.notificationservice.infrastructure.mail.dto.MailSendDto;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.BlackListRegisterEventMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.BlackListSendEmailMessage;
import com.ojosama.notificationservice.infrastructure.messaging.kafka.dto.TargetBlindEventMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationHandler {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final EmailLogRepository emailLogRepository;

    private final MailService mailService;

    //  블랙리스트 관리자 검토(관리자 알림)
    public void handleBlackListRequest(BlackListRegisterEventMessage message) {
        UUID adminId = userClient.getAdminInfo();
        notificationRepository.save(Notification.of(adminId, "운영알림", message.reason() + "로 블랙리스트 추가되었습니다.",
                TargetInfo.of(message.userId(), Target.OPERATION, TargetType.BLACKLIST_REGISTERED)));
    }

    // 블라인드 처리(각각의 카테고리 관리자에게 알림) -> 일단 ... 매니저가 한명인걸로 ,,
    public void handleBlindRegister(TargetBlindEventMessage message) {
        UUID managerId = userClient.getManagerInfo(message.categoryName());
        notificationRepository.save(Notification.of(managerId, "운영알림", message.targetType() + "에서 블라인드 처리되었습니다.",
                TargetInfo.of(message.targetId(), Target.OPERATION, TargetType.BLIND_REGISTERED)));
    }

    // 블랙리스트 사용자 이메일 전송
    public void handelSendBlackListEmail(BlackListSendEmailMessage message) {
        String email = userClient.getUserEmail(message.userId());
        try {
            MailSendDto mailSendDto = MailSendDto.of(email, "festie 알림", message.reason());
            mailService.sendEmail(mailSendDto);
            log.info("이메일 전송 성공 : {}", message.userId());
        } catch (NotificationException e) {
            log.error("이메일 전송 실패 : {}", message.userId());
            throw e;
        }
    }


}
