package com.ojosama.eventservice.eventrequest.application.dto.command;

import com.ojosama.eventservice.eventrequest.presentation.dto.request.CreateEventRequestRequest;
import java.util.UUID;

public record CreateEventRequestCommand(
        UUID requesterId,
        String eventName,
        UUID categoryId,
        String link,
        String description
) {
    public static CreateEventRequestCommand from(UUID requesterId, CreateEventRequestRequest request) {
        return new CreateEventRequestCommand(
                requesterId,
                request.title(),
                request.categoryId(),
                request.link(),
                request.description()
        );
    }
}
