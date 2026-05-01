package com.ojosama.community.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

//유저 블랙리스트 상태 변경 이벤트.
// 토픽: operation.blacklist.updated.v1
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserBlacklistStatusEvent(
        UUID userId,
        BlacklistStatus status
) {
}
