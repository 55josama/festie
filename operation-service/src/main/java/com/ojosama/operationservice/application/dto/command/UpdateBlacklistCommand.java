package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBlacklistCommand {
    private BlacklistStatus status;
    private String reason;
}
