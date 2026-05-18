package com.ojosama.notificationservice.application.handler;

import com.ojosama.notificationservice.application.command.BlackListRegisterCommand;
import com.ojosama.notificationservice.application.command.BlackListRequestCommand;
import com.ojosama.notificationservice.application.command.OperationRequestCommand;
import com.ojosama.notificationservice.application.command.TargetBlindEventCommand;
import com.ojosama.notificationservice.application.dto.result.NotificationResult;
import com.ojosama.notificationservice.domain.model.notification.Notification;
import com.ojosama.notificationservice.domain.model.notification.Target;
import com.ojosama.notificationservice.domain.model.notification.TargetInfo;
import com.ojosama.notificationservice.domain.model.notification.TargetType;
import com.ojosama.notificationservice.domain.repository.NotificationRepository;
import com.ojosama.notificationservice.infrastructure.client.UserClient;
import com.ojosama.notificationservice.infrastructure.mail.MailService;
import com.ojosama.notificationservice.infrastructure.mail.dto.MailSendDto;
import com.ojosama.notificationservice.infrastructure.sse.SseEmitterManager;
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
    private final MailService mailService;

    private final SseEmitterManager sseEmitterManager;

    //  블랙리스트 관리자 검토(관리자 알림)
    public void handleBlackListRequest(BlackListRequestCommand command) {
        UUID adminId = userClient.getAdminInfo();
        Notification notification = notificationRepository.save(
                Notification.of(adminId, "운영알림", command.reason() + "로 블랙리스트 추가되었습니다.",
                        TargetInfo.of(command.targetUserId(), Target.OPERATION, TargetType.BLACKLIST_REGISTERED)));

        sseEmitterManager.broadcast(adminId, NotificationResult.of(notification));
    }

    // 블라인드 처리(각각의 카테고리 관리자에게 알림) -> 일단 ... 매니저가 한명인걸로 ,,
    public void handleBlindRegister(TargetBlindEventCommand command) {
        log.info("매니저 정보 : {}", command.category());

        String category = command.category() != null ? command.category() : "COMMUNITY";

        UUID managerId = userClient.getManagerInfo(category);
        Notification notification = notificationRepository.save(
                Notification.of(managerId, "운영알림", command.targetType() + " 에서 블라인드 처리되었습니다.",
                        TargetInfo.of(command.targetId(), Target.OPERATION, TargetType.BLIND_REGISTERED)));

        sseEmitterManager.broadcast(managerId, NotificationResult.of(notification));

    }

    // 블랙리스트 사용자 이메일 전송
    public void handleBlackListRegister(BlackListRegisterCommand command) {
        String email = userClient.getUserEmail(command.userId());
        MailSendDto mailSendDto = MailSendDto.of(email, "festie 알림", command.reason());
        mailService.sendEmail(mailSendDto);
        log.info("이메일 전송 성공 : {}", command.userId());
    }

    // 운영요청 알림 관리자,,
    public void handleOperationRequest(OperationRequestCommand command) {
        log.info("요청자 정보 : {}", command.requesterId());
        UUID adminId = userClient.getAdminInfo();
        Notification notification = notificationRepository.save(
                Notification.of(adminId, "운영알림", command.title() + " 요청이 들어왔습니다.",
                        TargetInfo.operation(command.requestId())));

        sseEmitterManager.broadcast(adminId, NotificationResult.of(notification));
    }


}
