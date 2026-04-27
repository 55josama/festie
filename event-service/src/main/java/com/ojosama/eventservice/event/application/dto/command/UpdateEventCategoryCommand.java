package com.ojosama.eventservice.event.application.dto.command;

import java.util.UUID;

public record UpdateEventCategoryCommand(UUID userId, UUID categoryId, String name) {}
