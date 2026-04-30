package com.ojosama.chatbot.application.service;

import com.ojosama.chatbot.domain.exception.AiChatErrorCode;
import com.ojosama.chatbot.domain.exception.AiChatException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final ChatClient chatClient;

    public String askQuestion(String question) {
        try {
            return chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
        } catch (RuntimeException e) {
            throw new AiChatException(AiChatErrorCode.OPEN_AI_API_COMMUNICATION_FAILURE);
        }
    }
}
