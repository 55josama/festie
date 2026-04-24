package com.ojosama.favoriteservice.application.service;

import com.ojosama.common.exception.CustomException;
import com.ojosama.favoriteservice.application.dto.command.CreateFavoriteCommand;
import com.ojosama.favoriteservice.application.dto.result.CreateFavoriteResult;
import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public CreateFavoriteResult createFavorite(CreateFavoriteCommand command) {

        Favorite favorite;
        try {
            favorite = favoriteRepository.save(
                    Favorite.of(command.userId(), command.eventId(), command.categoryId()));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(FavoriteErrorCode.EXIST_FAVORITE);
        }

        // 나중에 feign 수정 예정
        String eventName = "콘서트";
        String userName = "사용자이름";

        return CreateFavoriteResult.of(favorite, eventName, userName);
    }

    public void deleteFavorite(UUID favoriteId, UUID userId) {
        Favorite favorite = favoriteRepository.findByIdAndUserIdAndDeletedAtIsNull(favoriteId, userId).orElseThrow(() ->
                new CustomException(FavoriteErrorCode.FAVORITE_NOT_FOUND));

        favorite.delete(favoriteId);
    }
}
