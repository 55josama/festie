package com.ojosama.eventservice.event.application.dto.result;

import com.ojosama.eventservice.event.infrastructure.client.dto.ChatRoomSummaryDto;

public record EventDetailResult(
        EventResult event,
        ChatRoomSummaryDto chatRoom
) {}
