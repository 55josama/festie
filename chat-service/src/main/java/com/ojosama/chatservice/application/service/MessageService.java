package com.ojosama.chatservice.application.service;

import static com.ojosama.chatservice.infrastructure.messaging.kafka.dto.AiModerationRequestEvent.from;

import com.ojosama.chatservice.application.dto.command.ChangeMessageStatusCommand;
import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.command.DeleteMessageCommand;
import com.ojosama.chatservice.application.dto.query.FindAdminMessagesQuery;
import com.ojosama.chatservice.application.dto.query.FindMessageQuery;
import com.ojosama.chatservice.application.dto.query.FindMessagesByChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.dto.result.ReportedMessageResult;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.chatservice.domain.repository.MessageRepository;
import com.ojosama.chatservice.infrastructure.client.UserClient;
import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.kafka.domain.EventType;
import com.ojosama.common.kafka.domain.OutboxEventPublisher;
import feign.FeignException;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private static final int MAX_MESSAGE_CONTENT_LENGTH = 1000;
    private static final int MAX_WRITER_NICKNAME_LENGTH = 50;
    private static final int MAX_PAGE_SIZE = 100;
    private static final UUID SYSTEM_BLINDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String AGGREGATE_TYPE = "CHAT";

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserClient userClient;
    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${spring.kafka.topic.chat-moderation-requested}")
    private String chatModerationRequestedTopic;

    public MessageResult createMessage(CreateMessageCommand command) {
        if (command == null || command.chatRoomId() == null || command.userId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        validateContent(command.content());

        ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.isOpen()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_OPEN);
        }

        String writerNickname = resolveWriterNickname(command.userId());
        validateWriterNickname(writerNickname);

        Message message = Message.builder()
                .chatRoomId(command.chatRoomId())
                .userId(command.userId())
                .writerNickname(writerNickname.trim())
                .content(command.content().trim())
                .build();

        Message savedMessage = messageRepository.save(message);
        outboxEventPublisher.publish(
                AGGREGATE_TYPE,
                savedMessage.getId(),
                EventType.CHAT_MODERATION_REQUESTED,
                chatModerationRequestedTopic,
                from(savedMessage) // ai 검증 dto 의 from
        );

        return MessageResult.from(savedMessage);
    }

    @Transactional(readOnly = true)
    public MessageResult getMessage(FindMessageQuery query) {
        if (query == null || query.messageId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        Message message = findMessage(query.messageId());
        if (!message.isVisible()) {
            throw new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND);
        }
        return MessageResult.from(message);
    }

    @Transactional(readOnly = true)
    public MessageSliceResult getMessagesByChatRoom(FindMessagesByChatRoomQuery query) {
        if (query == null || query.chatRoomId() == null || query.page() < 0
                || query.size() <= 0 || query.size() > MAX_PAGE_SIZE) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        findChatRoom(query.chatRoomId());
        return getMessageSlice(
                messageRepository.findByChatRoomIdAndStatuses(
                        query.chatRoomId(),
                        List.copyOf(EnumSet.of(MessageStatus.ACTIVE, MessageStatus.BLINDED)),
                        PageRequest.of(query.page(), query.size())
                )
        );
    }

    @Transactional(readOnly = true)
    public MessageSliceResult getMessagesForAdmin(FindAdminMessagesQuery query) {
        if (query == null || query.page() < 0 || query.size() <= 0 || query.size() > MAX_PAGE_SIZE) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        Pageable pageable = PageRequest.of(query.page(), query.size());
        Slice<Message> messages = messageRepository.findByStatusesAndCategory(
                resolveAdminStatuses(query.status()),
                query.category(),
                pageable
        );
        return getMessageSlice(messages);
    }

    public void deleteMessage(DeleteMessageCommand command) {
        if (command == null || command.messageId() == null || command.userId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        Message message = findMessage(command.messageId());
        if (!command.userId().equals(message.getUserId())) {
            throw new ChatException(ChatErrorCode.MESSAGE_DELETE_FORBIDDEN);
        }

        message.delete();
    }

    public MessageResult changeMessageStatus(ChangeMessageStatusCommand command) {
        if (command == null || command.messageId() == null || command.adminId() == null || command.status() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        Message message = findMessage(command.messageId());
        if (command.status() == MessageStatus.BLINDED) {
            message.blind(command.adminId());
        } else if (command.status() == MessageStatus.ACTIVE) {
            message.unblind(command.adminId());
        } else {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return MessageResult.from(message);
    }

    public void blindMessageBySystem(UUID messageId) {
        if (messageId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        changeMessageStatus(new ChangeMessageStatusCommand(messageId, SYSTEM_BLINDER_ID, MessageStatus.BLINDED));
    }

    public void unblindMessageBySystem(UUID messageId) {
        if (messageId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        changeMessageStatus(new ChangeMessageStatusCommand(messageId, SYSTEM_BLINDER_ID, MessageStatus.ACTIVE));
    }

    @Transactional(readOnly = true)
    public ReportedMessageResult getReportedMessage(UUID messageId) {
        if (messageId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        Message message = findMessage(messageId);
        ChatRoom chatRoom = findChatRoom(message.getChatRoomId());

        return ReportedMessageResult.from(message, chatRoom.getCategory().name());
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ChatException(ChatErrorCode.MESSAGE_CONTENT_REQUIRED);
        }
        if (content.trim().length() > MAX_MESSAGE_CONTENT_LENGTH) {
            throw new ChatException(ChatErrorCode.MESSAGE_CONTENT_TOO_LONG);
        }
    }

    private void validateWriterNickname(String writerNickname) {
        if (writerNickname == null || writerNickname.isBlank()) {
            throw new ChatException(ChatErrorCode.MESSAGE_USER_NICKNAME_EMPTY);
        }
        if (writerNickname.trim().length() > MAX_WRITER_NICKNAME_LENGTH) {
            throw new ChatException(ChatErrorCode.MESSAGE_WRITER_NICKNAME_TOO_LONG);
        }
    }

    private String resolveWriterNickname(UUID userId) {
        try {
            var response = userClient.getInternalUserNickname(userId);
            if (response == null || response.nickname() == null || response.nickname().isBlank()) {
                throw new ChatException(ChatErrorCode.MESSAGE_USER_NICKNAME_EMPTY);
            }
            return response.nickname();
        } catch (FeignException e) {
            throw new ChatException(ChatErrorCode.MESSAGE_USER_NICKNAME_FETCH_FAILED);
        }
    }

    private Message findMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND));
    }

    private ChatRoom findChatRoom(UUID chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private MessageSliceResult getMessageSlice(Slice<Message> messages) {
        List<MessageResult> results = messages.getContent().stream()
                .map(MessageResult::from)
                .toList();
        return new MessageSliceResult(results, messages.hasNext());
    }

    private List<MessageStatus> resolveAdminStatuses(MessageStatus status) {
        if (status == null) {
            return List.copyOf(EnumSet.allOf(MessageStatus.class));
        }
        return List.of(status);
    }
}
