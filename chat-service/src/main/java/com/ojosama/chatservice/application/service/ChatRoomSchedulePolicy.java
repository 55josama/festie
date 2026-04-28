package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.domain.exception.ChatErrorCode;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.domain.model.ChatRoomSchedule;
import com.ojosama.common.exception.CommonErrorCode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomSchedulePolicy {

    public ChatRoomSchedule calculate(LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        if (eventStartAt == null || eventEndAt == null) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }
        if (eventEndAt.isBefore(eventStartAt)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_INVALID_TIME);
        }
        LocalDateTime openAt = eventStartAt.toLocalDate().atStartOfDay();
        LocalDateTime closeAt = eventEndAt.plusHours(1);
        return new ChatRoomSchedule(openAt, closeAt);
    }
}
