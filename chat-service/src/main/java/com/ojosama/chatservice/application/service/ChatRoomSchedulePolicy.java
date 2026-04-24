package com.ojosama.chatservice.application.service;

import com.ojosama.chatservice.domain.model.ChatRoomSchedule;
import java.time.LocalDateTime;

public class ChatRoomSchedulePolicy {

    public ChatRoomSchedule calculate(LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        LocalDateTime openAt = eventStartAt.toLocalDate().atStartOfDay();
        LocalDateTime closeAt = eventEndAt.plusHours(1);
        return new ChatRoomSchedule(openAt, closeAt);
    }
}