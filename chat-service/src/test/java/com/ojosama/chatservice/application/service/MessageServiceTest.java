package com.ojosama.chatservice.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.chatservice.domain.repository.MessageRepository;
import com.ojosama.chatservice.infrastructure.client.UserClient;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(messageService, "chatModerationRequestedTopic", "chat-moderation-requested");
    }

    @Test
    void createMessage_shouldRejectBannedWordBypassBeforeSaving() {
        CreateMessageCommand command = new CreateMessageCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "시ㅣㅣㅣ발"
        );

        assertThatThrownBy(() -> messageService.createMessage(command))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.MESSAGE_BANNED_WORD_DETECTED.getMessage());

        verifyNoInteractions(messageRepository, chatRoomRepository, userClient, outboxEventPublisher, redisTemplate);
    }
}
