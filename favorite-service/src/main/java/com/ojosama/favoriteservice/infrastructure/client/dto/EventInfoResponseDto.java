package com.ojosama.favoriteservice.infrastructure.client.dto;

import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import java.time.LocalDateTime;

public record EventInfoResponseDto(
        String name,
        String img,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status
) {
    // 행사 상태가 완료 or 취소 된 행사는 찜 불가능
    public void validate() {
        if (status.equals("COMPLETED") || status.equals("CANCELLED")) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_STATUS);
        }
    }
}
