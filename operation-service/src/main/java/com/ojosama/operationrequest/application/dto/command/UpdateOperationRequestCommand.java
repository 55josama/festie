package com.ojosama.operationrequest.application.dto.command;

import java.util.UUID;

public record UpdateOperationRequestCommand(
        UUID requesterId,
        String title,
        String content
) { }
