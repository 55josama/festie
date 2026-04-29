package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.command.CreateMessageCommand;
import com.ojosama.chatservice.application.dto.command.DeleteMessageCommand;
import com.ojosama.chatservice.application.dto.query.FindMessageQuery;
import com.ojosama.chatservice.application.dto.query.FindMessagesByChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.MessageResult;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.dto.result.ReportedMessageResult;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.Message;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.chatservice.domain.repository.MessageRepository;
import com.ojosama.common.exception.CommonErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public MessageResult createMessage(CreateMessageCommand command) {
        if (command == null || command.chatRoomId() == null || command.userId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        validateWriterNickname(command.writerNickname());
        validateContent(command.content());

        ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.isOpen()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_OPEN);
        }

        Message message = Message.builder()
                .chatRoomId(command.chatRoomId())
                .userId(command.userId())
                .writerNickname(command.writerNickname().trim())
                .content(command.content().trim())
                .build();

        return MessageResult.from(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public MessageResult getMessage(FindMessageQuery query) {
        if (query == null || query.messageId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return MessageResult.from(findMessage(query.messageId()));
    }

    @Transactional(readOnly = true)
    public MessageSliceResult getMessagesByChatRoom(FindMessagesByChatRoomQuery query) {
        if (query == null || query.chatRoomId() == null || query.page() < 0 || query.size() <= 0) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        findChatRoom(query.chatRoomId());

        Pageable pageable = PageRequest.of(query.page(), query.size());
        Slice<Message> messages = messageRepository.findByChatRoomId(query.chatRoomId(), pageable);

        List<MessageResult> results = messages.getContent().stream()
                .map(MessageResult::from)
                .toList();

        return new MessageSliceResult(results, messages.hasNext());
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

    @Transactional(readOnly = true)
    public ReportedMessageResult getReportedMessage(UUID messageId) {
        if (messageId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        Message message = findMessage(messageId);
        ChatRoom chatRoom = findChatRoom(message.getChatRoomId());

        return ReportedMessageResult.from(message, chatRoom.getCategory());
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
            throw new ChatException(ChatErrorCode.MESSAGE_WRITER_NICKNAME_REQUIRED);
        }
        if (writerNickname.trim().length() > MAX_WRITER_NICKNAME_LENGTH) {
            throw new ChatException(ChatErrorCode.MESSAGE_WRITER_NICKNAME_TOO_LONG);
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
}
