package com.ojosama.operationservice.application.dto.command;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateBlacklistCommand {
    private UUID userId;
    private String reason;
}
