package com.ojosama.eventservice.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCategoryQueryService 단위 테스트")
class EventCategoryQueryServiceTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @InjectMocks
    private EventCategoryQueryServiceImpl eventCategoryQueryService;

    @Nested
    @DisplayName("카테고리 목록 조회")
    class GetCategories {

        @Test
        @DisplayName("성공: 전체 카테고리 목록을 반환한다")
        void getCategories_success() {
            given(eventCategoryRepository.findAll()).willReturn(List.of(
                    EventCategory.create("FESTIVAL"),
                    EventCategory.create("CONCERT")));

            List<EventCategoryResult> result = eventCategoryQueryService.getCategories();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(EventCategoryResult::name)
                    .containsExactly("FESTIVAL", "CONCERT");
        }

        @Test
        @DisplayName("성공: 카테고리가 없으면 빈 목록을 반환한다")
        void getCategories_empty() {
            given(eventCategoryRepository.findAll()).willReturn(List.of());

            List<EventCategoryResult> result = eventCategoryQueryService.getCategories();

            assertThat(result).isEmpty();
        }
    }
}
