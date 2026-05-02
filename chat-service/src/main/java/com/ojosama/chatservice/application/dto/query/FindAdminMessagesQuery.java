package com.ojosama.chatservice.application.dto.query;

import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.domain.model.MessageStatus;

public record FindAdminMessagesQuery(
        MessageStatus status,
        EventCategory category,
        int page,
        int size
) {
}
