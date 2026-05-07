package com.ojosama.chatservice.presentation.dto.response;

import com.ojosama.chatservice.application.dto.result.EventLocationVerificationResult;
import java.util.UUID;

public record EventLocationVerificationResponse(
        UUID eventId,
        boolean isNearEvent
) {
    public static EventLocationVerificationResponse from(EventLocationVerificationResult result) {
        return new EventLocationVerificationResponse(
                result.eventId(),
                result.nearEvent()
        );
    }
}
