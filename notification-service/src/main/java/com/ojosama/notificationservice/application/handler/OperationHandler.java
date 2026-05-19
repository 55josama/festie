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

        String blindContent = String.format(
                "[%s] %s 카테고리의 콘텐츠가 블라인드 처리되었습니다. (대상 ID: %s)",
                command.targetType(),
                category,
                command.targetId()
        );

        UUID managerId = userClient.getManagerInfo(category);
        Notification notification = notificationRepository.save(
                Notification.of(managerId, "운영알림", blindContent,
                        TargetInfo.of(command.targetId(), Target.OPERATION, TargetType.BLIND_REGISTERED)));

        sseEmitterManager.broadcast(managerId, NotificationResult.of(notification));

    }

    // 블랙리스트 사용자 이메일 전송
    public void handleBlackListRegister(BlackListRegisterCommand command) {
        String email = userClient.getUserEmail(command.userId())

        String customContent = String.format(
                "안녕하세요, Festie 운영팀입니다. 회원님의 계정이 '%s' 사유로 인해 블랙리스트에 등록되었음을 안내해 드립니다.",
                command.reason()
        );

        MailSendDto mailSendDto = MailSendDto.of(email, "[Festie] 서비스 이용 제한 안내", customContent);

        mailService.sendEmail(mailSendDto);

        log.info("이메일 전송 성공 : {}", command.userId());
    }

    // 운영요청 알림 관리자,,
    public void handleOperationRequest(OperationRequestCommand command) {
        log.info("요청자 정보 : {}", command.requesterId());

        String multiLineContent = String.format(
                """
                        새로운 운영 요청이 들어왔습니다.
                                 • 요청 내용: %s
                                 • 요청자 ID: %s""",
                command.title(),
                command.requesterId()
        );

        UUID adminId = userClient.getAdminInfo();
        Notification notification = notificationRepository.save(
                Notification.of(adminId, "운영알림", multiLineContent,
                        TargetInfo.operation(command.requestId())));

        sseEmitterManager.broadcast(adminId, NotificationResult.of(notification));
    }


}
