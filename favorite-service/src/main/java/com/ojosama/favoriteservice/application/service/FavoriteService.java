package com.ojosama.favoriteservice.application.service;

import com.ojosama.common.exception.CustomException;
import com.ojosama.favoriteservice.application.dto.command.CreateFavoriteCommand;
import com.ojosama.favoriteservice.application.dto.result.CreateFavoriteResult;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public CreateFavoriteResult createFavorite(CreateFavoriteCommand command) {

        Boolean isExists = favoriteRepository.existsByUserIdAndEventId(command.userId(), command.eventId());

        if (isExists) {
            throw new CustomException(FavoriteErrorCode.EXIST_FAVORITE);
        }

        Favorite favorite = favoriteRepository.save(
                Favorite.of(command.userId(), command.eventId(), command.categoryId()));

        // 나중에 feign 수정 예정
        String eventName = "콘서트";
        String userName = "사용자이름";

        return CreateFavoriteResult.of(favorite, eventName, userName);
    }

    public void deleteFavorite(UUID favoriteId) {
        Favorite favorite = favoriteRepository.findByIdAndDeletedAtIsNull(favoriteId).orElseThrow(() ->
                new CustomException(FavoriteErrorCode.FAVORITE_NOT_FOUND));

        favorite.delete(favoriteId);
    }
}
