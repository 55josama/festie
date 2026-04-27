package com.ojosama.moderation.infrastructure.client;

import com.ojosama.moderation.domain.event.payload.AiEvaluateEvent;
import com.ojosama.moderation.domain.event.payload.AiModerationRequestEvent;
import com.ojosama.moderation.infrastructure.client.dto.AiModerationClientResponse;
import com.ojosama.moderation.infrastructure.prompt.AiModerationPromptTemplate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class AiModerationClient {
    private final ChatClient chatClient;

    public AiModerationClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public List<AiModerationClientResponse> analyzeBatch(List<AiModerationRequestEvent> events) {
        // 이벤트 리스트를 AI가 읽기 좋게 문자열로 결합
        String userContent = events.stream()
                .map(e -> String.format("targetId: %s, content: %s", e.targetId(), e.content()))
                .collect(Collectors.joining("\n"));

        // 분리해둔 템플릿과 결합된 텍스트를 GPT에게 전송
        return chatClient.prompt()
                .system(AiModerationPromptTemplate.BATCH_MODERATION_SYSTEM_PROMPT)
                .user(userContent)
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
