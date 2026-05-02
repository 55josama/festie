package com.ojosama.favoriteservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteRequestDto;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteResponseDto;
import com.ojosama.favoriteservice.presentation.dto.GetFavoritesResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateFavoriteResponseDto>> createFavorite(
            @Valid @RequestBody CreateFavoriteRequestDto favoriteDto,
            @RequestHeader("X-User-Id") UUID userId) {

        FavoriteResult result = favoriteService.createFavorite(favoriteDto.toCommand(), userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        CreateFavoriteResponseDto.of(result)));

    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(@PathVariable("favoriteId") UUID favoriteId,
                                                            @RequestHeader("X-User-Id") UUID userId) {

        favoriteService.deleteFavorite(favoriteId, userId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetFavoritesResponseDto>>> getFavorites(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 10,
                    sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<GetFavoritesResponseDto> dto = favoriteService.getFavorites(userId, pageable)
                .map(GetFavoritesResponseDto::from);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
