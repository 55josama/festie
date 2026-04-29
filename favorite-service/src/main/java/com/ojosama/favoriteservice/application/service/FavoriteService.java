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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

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

    public List<FavoriteResult> getFavorites(UUID userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdAndDeletedAtIsNull(userId);

        return favorites.stream()
                .map(FavoriteResult::from)
                .toList();
    }

    public void deleteAllByEventId(UUID eventId) {
        List<Favorite> favorites = favoriteRepository.findByEventInfo_EventIdAndDeletedAtIsNull(eventId);
        favorites.forEach(favorite -> favorite.deleted(null));
    }
}
