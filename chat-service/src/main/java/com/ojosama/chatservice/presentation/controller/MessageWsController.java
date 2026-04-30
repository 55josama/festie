package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.presentation.dto.request.SendMessageWsRequest;
import com.ojosama.chatservice.presentation.dto.response.MessageWsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageWsController {

    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";
    private static final String ROOM_TOPIC_SUFFIX = "/messages";

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendMessageWsRequest request) {
        MessageResult result = messageService.createMessage(
                new CreateMessageCommand(
                        request.chatRoomId(),
                        request.userId(),
                        request.writerNickname(),
                        request.content()
                )
        );

        messagingTemplate.convertAndSend(
                ROOM_TOPIC_PREFIX + request.chatRoomId() + ROOM_TOPIC_SUFFIX,
                MessageWsResponse.from(result)
        );
    }
}
