package com.ojosama.favoriteservice.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ojosama.favoriteservice.application.dto.command.CreateFavoriteCommand;
import com.ojosama.favoriteservice.application.dto.result.FavoriteResult;
import com.ojosama.favoriteservice.application.service.FavoriteService;
import com.ojosama.favoriteservice.domain.model.Favorite;
import com.ojosama.favoriteservice.domain.repository.FavoriteRepository;
import com.ojosama.favoriteservice.infrastructure.client.EventClient;
import com.ojosama.favoriteservice.infrastructure.client.dto.EventInfoResponseDto;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private EventClient eventClient;

    private UUID userId;
    private UUID eventId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("찜 생성 성공")
    void createFavorite_success() {
        CreateFavoriteCommand command = new CreateFavoriteCommand(eventId, categoryId);
        EventInfoResponseDto eventInfo = new EventInfoResponseDto("콘서트", "url");

        given(favoriteRepository.findByEventInfo_EventIdAndUserId(eventId, userId))
                .willReturn(Optional.empty());
        given(eventClient.getEvents(eventId)).willReturn(eventInfo);
        given(favoriteRepository.save(any(Favorite.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        FavoriteResult result = favoriteService.createFavorite(command, userId);

        assertThat(result.eventId()).isEqualTo(eventId);
        assertThat(result.eventName()).isEqualTo("콘서트");
        assertThat(result.userId()).isEqualTo(userId);
    }
}
