package com.ojosama.eventservice.event.application.dto.command;

import java.util.UUID;

public record CreateEventCategoryCommand(
    UUID userId,
    String name
) {}
