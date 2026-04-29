package com.ojosama.eventservice.event.domain.support;

import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventSnapshot {
    private UUID id;
    private String name;
    private UUID categoryId;
    private String categoryName;
    private EventTime eventTime;
    private EventLocation eventLocation;
    private EventFee eventFee;
    private EventTicketing eventTicketing;
    private String officialLink;
    private String description;
    private String performer;
    private String img;
    private EventStatus status;

    // Event 엔티티로부터 현재 상태의 스냅샷을 생성
    public static EventSnapshot from(Event event) {
        return EventSnapshot.builder()
                .id(event.getId())
                .name(event.getName())
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .eventTime(event.getEventTime())
                .eventLocation(event.getEventLocation())
                .eventFee(event.getEventFee())
                .eventTicketing(event.getEventTicketing())
                .officialLink(event.getOfficialLink())
                .description(event.getDescription())
                .performer(event.getPerformer())
                .img(event.getImg())
                .status(event.getStatus())
                .build();
    }

    // 두 스냅샷을 비교하여 변경된 필드들을 추적
    public static EventChanges compareSnapshots(EventSnapshot before, EventSnapshot after) {
        return new EventChanges(before, after);
    }
}