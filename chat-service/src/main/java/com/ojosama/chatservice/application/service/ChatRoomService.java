package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.command.CloseChatRoomCommand;
import com.ojosama.chatservice.application.dto.command.CreateChatRoomCommand;
import com.ojosama.chatservice.application.dto.command.ForceCloseChatRoomCommand;
import com.ojosama.chatservice.application.dto.command.OpenChatRoomCommand;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomSchedule;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.common.exception.CommonErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomResult createChatRoom(CreateChatRoomCommand command) {
        if (command == null
                || command.eventId() == null
                || command.category() == null
                || command.scheduledOpenAt() == null
                || command.scheduledCloseAt() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (chatRoomRepository.findByEventId(command.eventId()).isPresent()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .eventId(command.eventId())
                .category(command.category())
                .schedule(new ChatRoomSchedule(command.scheduledOpenAt(), command.scheduledCloseAt()))
                .build();

        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    @Transactional(readOnly = true)
    public ChatRoomResult getChatRoom(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return ChatRoomResult.from(chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ChatRoomResult getChatRoomByEventId(UUID eventId) {
        if (eventId == null) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_EVENT_ID_REQUIRED);
        }
        return ChatRoomResult.from(chatRoomRepository.findByEventId(eventId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND)));
    }

    public ChatRoomResult openChatRoom(OpenChatRoomCommand command) {
        if (command == null || command.chatRoomId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        ChatRoom chatRoom = getChatRoomEntity(command.chatRoomId());
        validateCanOpen(chatRoom);
        chatRoom.open();
        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    public ChatRoomResult closeChatRoom(CloseChatRoomCommand command) {
        if (command == null || command.chatRoomId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        ChatRoom chatRoom = getChatRoomEntity(command.chatRoomId());
        validateCanClose(chatRoom);
        chatRoom.close();
        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    public ChatRoomResult forceCloseChatRoom(ForceCloseChatRoomCommand command) {
        if (command == null || command.chatRoomId() == null || command.adminId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        ChatRoom chatRoom = getChatRoomEntity(command.chatRoomId());
        if (chatRoom.isClosed()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_ENDED);
        }
        chatRoom.forceClose(command.adminId());
        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    public int openScheduledChatRooms(LocalDateTime now) {
        List<ChatRoom> chatRooms = chatRoomRepository.findScheduledToOpen(now);
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.open();
        }
        return chatRooms.size();
    }

    public int closeScheduledChatRooms(LocalDateTime now) {
        List<ChatRoom> chatRooms = chatRoomRepository.findScheduledToClose(now);
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.close();
        }
        return chatRooms.size();
    }

    // 내부 메서드
    private ChatRoom getChatRoomEntity(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void validateCanOpen(ChatRoom chatRoom) {
        if (chatRoom.isOpen()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_OPENED);
        }
        if (chatRoom.isClosed()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_ENDED);
        }
    }

    private void validateCanClose(ChatRoom chatRoom) {
        if (chatRoom.isClosed()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_ENDED);
        }
        if (!chatRoom.isOpen()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_STATUS_INVALID);
        }
    }
}
