package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateBlacklistCommand {
    private UUID userId;
    private String reason;

    public Blacklist toEntity(){
        return Blacklist.of(userId, reason);
    }
}
