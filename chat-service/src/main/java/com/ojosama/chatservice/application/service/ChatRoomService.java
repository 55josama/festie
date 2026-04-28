package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.application.dto.command.ChangeChatRoomStatusCommand;
import com.ojosama.chatservice.application.dto.command.CreateChatRoomCommand;
import com.ojosama.chatservice.application.dto.query.FindChatRoomByEventIdQuery;
import com.ojosama.chatservice.application.dto.query.FindChatRoomQuery;
import com.ojosama.chatservice.application.dto.result.ChatRoomResult;
import com.ojosama.chatservice.application.dto.result.ChatRoomSummaryResult;
import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomSchedule;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import com.ojosama.common.exception.CommonErrorCode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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
                || command.eventName() == null || command.eventName().isBlank()
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
                .eventName(command.eventName())
                .category(command.category())
                .schedule(new ChatRoomSchedule(command.scheduledOpenAt(), command.scheduledCloseAt()))
                .build();
        try {
            return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateEventIdViolation(e)) {
                throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ChatRoomResult getChatRoom(FindChatRoomQuery query) {
        if (query == null || query.chatRoomId() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return ChatRoomResult.from(findChatRoomEntity(query.chatRoomId()));
    }

    @Transactional(readOnly = true)
    public ChatRoomResult getChatRoomByEventId(FindChatRoomByEventIdQuery query) {
        if (query == null || query.eventId() == null) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_EVENT_ID_REQUIRED);
        }
        return ChatRoomResult.from(chatRoomRepository.findByEventId(query.eventId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ChatRoomSummaryResult getChatRoomSummaryByEventId(FindChatRoomByEventIdQuery query) {
        if (query == null || query.eventId() == null) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_EVENT_ID_REQUIRED);
        }
        return chatRoomRepository.findByEventId(query.eventId())
                .map(ChatRoomSummaryResult::from)
                .orElseGet(ChatRoomSummaryResult::empty);
    }

    // 상태 분기 : 상태 변경 요청 API 가 들어왔을 때
    public ChatRoomResult changeChatRoomStatus(ChangeChatRoomStatusCommand command) {
        if (command == null || command.chatRoomId() == null || command.action() == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        ChatRoom chatRoom = findChatRoomEntity(command.chatRoomId());

        return switch (command.action()) {
            case FORCE_OPEN -> forceOpenChatRoom(chatRoom, command.adminId());
            case FORCE_CLOSE -> forceCloseChatRoom(chatRoom, command.adminId());
        };
    }

    // 자동 오픈
    public int openScheduledChatRooms(LocalDateTime now) {
        List<ChatRoom> chatRooms = chatRoomRepository.findScheduledToOpen(now);
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.open();
        }
        return chatRooms.size();
    }

    // 자동 종료
    public int closeScheduledChatRooms(LocalDateTime now) {
        List<ChatRoom> chatRooms = chatRoomRepository.findScheduledToClose(now);
        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.close();
        }
        return chatRooms.size();
    }

    private ChatRoomResult forceOpenChatRoom(ChatRoom chatRoom, UUID adminId) {
        if (adminId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (chatRoom.isOpen()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_OPENED);
        }
        chatRoom.forceOpen(adminId);
        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    private ChatRoomResult forceCloseChatRoom(ChatRoom chatRoom, UUID adminId) {
        if (adminId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (chatRoom.isClosed()) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_ALREADY_ENDED);
        }
        chatRoom.forceClose(adminId);
        return ChatRoomResult.from(chatRoomRepository.save(chatRoom));
    }

    private ChatRoom findChatRoomEntity(UUID chatRoomId) {
        if (chatRoomId == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private boolean isDuplicateEventIdViolation(DataIntegrityViolationException e) {
        // eventId 유니크 제약 위반만 "채팅방 이미 존재"로 해석
        // 다른 무결성 오류까지 중복 생성으로 오인하지 않도록, 제약명과 SQLState를 함께 확인
        Throwable cause = e;
        while (cause != null) {
            // ChatRoom 의 유니크 제약 확인
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                if ("uk_chat_room_event_id".equals(constraintViolationException.getConstraintName())) {
                    return true;
                }
            }
            // PostgreSQL에서 unique key 충돌 제약 23505
            if (cause instanceof SQLException sqlException) {
                if ("23505".equals(sqlException.getSQLState())
                        && isEventIdConstraintMessage(sqlException.getMessage())) {
                    return true;
                }
            }
            String message = cause.getMessage();
            if (message != null && isEventIdConstraintMessage(message)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean isEventIdConstraintMessage(String message) {
        // DB 드라이버/버전에 따라 예외 메시지 포맷이 조금씩 달라질 수 있어서
        // constraint name / SQLState를 우선 보고, 이 문자열 체크는 보조 수단으로 사용
        String normalized = message.toLowerCase();
        return normalized.contains("uk_chat_room_event_id")
                || normalized.contains("p_chat_room_event_id_key")
                || (normalized.contains("event_id")
                && normalized.contains("duplicate key"));
    }

}
