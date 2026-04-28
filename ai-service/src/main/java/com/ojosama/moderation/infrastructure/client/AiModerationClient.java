package com.ojosama.moderation.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public AiModerationClient(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public List<AiModerationClientResponse> analyzeBatch(List<AiModerationRequestEvent> events) {
        // 이벤트 리스트를 AI가 읽기 좋게 JSON Array 형태로 결합
        String userContent;
        try {
            userContent = objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("배치 요청 데이터를 JSON으로 변환하는 데 실패했습니다.", e);
        }

        // 분리해둔 템플릿과 결합된 텍스트를 GPT에게 전송
        return chatClient.prompt()
                .system(AiModerationPromptTemplate.BATCH_MODERATION_SYSTEM_PROMPT)
                .user(userContent)
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
