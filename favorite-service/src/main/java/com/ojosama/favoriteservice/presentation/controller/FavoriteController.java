package com.ojosama.favoriteservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.common.response.PageResponse;
import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteRequestDto;
import com.ojosama.favoriteservice.presentation.dto.CreateFavoriteResponseDto;
import com.ojosama.favoriteservice.presentation.dto.GetFavoritesResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "찜", description = "찜 관리 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "찜 생성", description = "원하는 행사를 찜 할 수 있습니다.")
    public ResponseEntity<ApiResponse<CreateFavoriteResponseDto>> createFavorite(
            @Valid @RequestBody CreateFavoriteRequestDto favoriteDto,
            @AuthenticationPrincipal UUID userId) {

        FavoriteResult result = favoriteService.createFavorite(favoriteDto.toCommand(), userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        CreateFavoriteResponseDto.of(result)));

    }

    @DeleteMapping("/{favoriteId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "찜 삭제", description = "필요없는 찜을 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(@PathVariable("favoriteId") UUID favoriteId,
                                                            @AuthenticationPrincipal UUID userId) {

        favoriteService.deleteFavorite(favoriteId, userId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "찜 조회", description = "내가 생성한 찜을 조회 할 수 있습니다.")
    public ResponseEntity<ApiResponse<PageResponse<GetFavoritesResponseDto>>> getFavorites(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(
                    sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<GetFavoritesResponseDto> dto = PageResponse.from(favoriteService.getFavorites(userId, pageable)
                .map(GetFavoritesResponseDto::from));

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
