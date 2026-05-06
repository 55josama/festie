package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.presentation.dto.request.SendMessageWsRequest;
import com.ojosama.chatservice.presentation.dto.response.MessageWsResponse;
import com.ojosama.chatservice.presentation.dto.response.WebSocketErrorResponse;
import com.ojosama.common.exception.CommonErrorCode;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWsController {

    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";
    private static final String ROOM_TOPIC_SUFFIX = "/messages";

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendMessageWsRequest request, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        UUID userId = UUID.fromString(principal.getName());
        MessageResult result = messageService.createMessage(
                new CreateMessageCommand(
                        request.chatRoomId(),
                        userId,
                        request.content()
                )
        );

        messagingTemplate.convertAndSend(
                ROOM_TOPIC_PREFIX + request.chatRoomId() + ROOM_TOPIC_SUFFIX,
                MessageWsResponse.from(result)
        );
    }

    @MessageExceptionHandler(ChatException.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleChatException(ChatException e) {
        log.warn("웹소켓 비즈니스 예외: status={}, message={}", e.getStatus().value(), e.getMessage());
        return WebSocketErrorResponse.of(e.getStatus().value(), e.getMessage());
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        return WebSocketErrorResponse.of(
                CommonErrorCode.VALIDATION_ERROR.getStatus().value(),
                CommonErrorCode.VALIDATION_ERROR.getMessage()
        );
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleUnexpectedException(Exception e) {
        log.error("웹소켓 메시지 처리 중 예외가 발생했습니다.", e);
        return WebSocketErrorResponse.of(
                CommonErrorCode.UNEXPECTED_ERROR.getStatus().value(),
                CommonErrorCode.UNEXPECTED_ERROR.getMessage()
        );
    }
}
