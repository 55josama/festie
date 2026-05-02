package com.ojosama.favoriteservice.application.service;

import com.ojosama.favoriteservice.application.dto.command.CreateFavoriteCommand;
import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import com.ojosama.favoriteservice.domain.model.EventInfo;
import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import com.ojosama.favoriteservice.infrastructure.client.EventClient;
import com.ojosama.favoriteservice.infrastructure.client.dto.EventInfoResponseDto;
import com.ojosama.favoriteservice.infrastructure.messaging.kafka.dto.EventUpdatedMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FavoriteService {

    private static final UUID system = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final FavoriteRepository favoriteRepository;
    private final EventClient eventClient;

    public FavoriteResult createFavorite(CreateFavoriteCommand command, UUID userId) {
        // 존재하는 찜인지 확인
        Optional<Favorite> favoriteOpt = favoriteRepository.findByEventInfo_EventIdAndUserId(command.eventId(), userId);
        Favorite favorite;

        // 삭제 안 된 찜 -> 예외
        if (favoriteOpt.isPresent() && favoriteOpt.get().getDeletedAt() == null) {
            throw new FavoriteException(FavoriteErrorCode.EXIST_FAVORITE);
        }

        // feign을 통해서 event 정보 가져옴.
        EventInfoResponseDto dto = eventClient.getEvents(command.eventId());
        if (favoriteOpt.isPresent()) {
            favorite = favoriteOpt.get();
            favorite.restore(new EventInfo(command.eventId(), dto.eventName(), dto.imageUrl()),
                    command.categoryId());
        } else {
            favorite = favoriteRepository.save(
                    Favorite.of(userId, new EventInfo(command.eventId(), dto.eventName(), dto.imageUrl()),
                            command.categoryId()));
        }
        return FavoriteResult.from(favorite);
    }

    public void deleteFavorite(UUID favoriteId, UUID userId) {
        Favorite favorite = favoriteRepository.findByIdAndUserIdAndDeletedAtIsNull(favoriteId, userId).orElseThrow(() ->
                new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND));

        favorite.deleted(userId);
    }

    public Page<FavoriteResult> getFavorites(UUID userId, Pageable pageable) {
        Page<Favorite> favorites = favoriteRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);

        return favorites.map(FavoriteResult::from);
    }

    // 이벤트를 통한 행사 삭제
    public void deleteAllByEventId(UUID eventId) {
        List<Favorite> favorites = favoriteRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
        favorites.forEach(favorite -> favorite.deleted(system));
    }

    // 이벤트를 통한 행사 변경
    public void updateAllByEventId(UUID eventId, EventUpdatedMessage message) {
        if (message == null || message.changedFields() == null || message.changedFields().isEmpty()) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }

        for (var field : message.changedFields()) {
            String fieldName = field.fieldName();
            String after = field.after();
            favoriteRepository.updateEventInfoBulk(eventId, fieldName, after);
        }
    }
}
