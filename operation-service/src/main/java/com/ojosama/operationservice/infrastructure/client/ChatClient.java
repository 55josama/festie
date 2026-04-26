package com.ojosama.operationservice.infrastructure.client;

import com.ojosama.operationservice.infrastructure.client.dto.ChatMessageClientResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "chat-service")
public interface ChatClient {
    @GetMapping("/internal/v1/chat/messages/{messageId}")
    ChatMessageClientResponse getChatMessageDetail(@PathVariable("messageId") UUID userId);
}
