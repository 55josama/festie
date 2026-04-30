package com.ojosama.favoriteservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteRequestDto;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteResponseDto;
import com.ojosama.favoriteservice.presentation.dto.GetFavoritesResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateFavoriteResponseDto>> createFavorite(
            @Valid @RequestBody CreateFavoriteRequestDto favoriteDto) {

        // TODO : 수정예정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        FavoriteResult result = favoriteService.createFavorite(favoriteDto.toCommand(), userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        CreateFavoriteResponseDto.of(result)));

    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(@PathVariable("favoriteId") UUID favoriteId) {

        // TODO : 수정예정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");
        favoriteService.deleteFavorite(favoriteId, userId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetFavoritesResponseDto>>> getFavorites() {

        // TODO : 수정예정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");
        List<GetFavoritesResponseDto> dto = favoriteService.getFavorites(userId).stream()
                .map(GetFavoritesResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
