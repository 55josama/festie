package com.ojosama.eventservice.event.domain.support;

import java.util.ArrayList;
import java.util.List;

public class EventChanges {
    private final EventSnapshot before;
    private final EventSnapshot after;
    private final List<FieldChange> changedFields;

    public EventChanges(EventSnapshot before, EventSnapshot after) {
        this.before = before;
        this.after = after;
        this.changedFields = detectChanges();
    }

    // 변경된 필드들을 감지하고 FieldChange 리스트로 반환
    private List<FieldChange> detectChanges() {
        List<FieldChange> changes = new ArrayList<>();

        // 기본 필드들
        addChangeIfDifferent(changes, "name", before.getName(), after.getName());
        addChangeIfDifferent(changes, "category", before.getCategoryName(), after.getCategoryName());
        addChangeIfDifferent(changes, "description", before.getDescription(), after.getDescription());
        addChangeIfDifferent(changes, "performer", before.getPerformer(), after.getPerformer());
        addChangeIfDifferent(changes, "img", before.getImg(), after.getImg());
        addChangeIfDifferent(changes, "officialLink", before.getOfficialLink(), after.getOfficialLink());
        addChangeIfDifferent(changes, "status", before.getStatus(), after.getStatus());

        // EventTime 필드들

        addChangeIfDifferent(changes, "startAt",
                before.getEventTime() == null ? null : before.getEventTime().getStartAt(),
                after.getEventTime() == null ? null : after.getEventTime().getStartAt());
        addChangeIfDifferent(changes, "endAt",
                before.getEventTime() == null ? null : before.getEventTime().getEndAt(),
                after.getEventTime() == null ? null : after.getEventTime().getEndAt());

        // EventLocation 필드들
        addChangeIfDifferent(changes, "place",
                before.getEventLocation() == null ? null : before.getEventLocation().getPlace(),
                after.getEventLocation() == null ? null : after.getEventLocation().getPlace());
        addChangeIfDifferent(changes, "latitude",
                before.getEventLocation() == null ? null : before.getEventLocation().getLatitude(),
                after.getEventLocation() == null ? null : after.getEventLocation().getLatitude());
        addChangeIfDifferent(changes, "longitude",
                before.getEventLocation() == null ? null : before.getEventLocation().getLongitude(),
                after.getEventLocation() == null ? null : after.getEventLocation().getLongitude());

        // EventFee 필드들
        addChangeIfDifferent(changes, "minFee",
                before.getEventFee() == null ? null : before.getEventFee().getMinFee(),
                after.getEventFee() == null ? null : after.getEventFee().getMinFee());
        addChangeIfDifferent(changes, "maxFee",
                before.getEventFee() == null ? null : before.getEventFee().getMaxFee(),
                after.getEventFee() == null ? null : after.getEventFee().getMaxFee());

        // EventTicketing 필드들
        addChangeIfDifferent(changes, "hasTicketing",
                before.getEventTicketing() == null ? null : before.getEventTicketing().getHasTicketing(),
                after.getEventTicketing() == null ? null : after.getEventTicketing().getHasTicketing());
        addChangeIfDifferent(changes, "ticketingOpenAt",
                before.getEventTicketing() == null ? null : before.getEventTicketing().getTicketingOpenAt(),
                after.getEventTicketing() == null ? null : after.getEventTicketing().getTicketingOpenAt());
        addChangeIfDifferent(changes, "ticketingCloseAt",
                before.getEventTicketing() == null ? null : before.getEventTicketing().getTicketingCloseAt(),
                after.getEventTicketing() == null ? null : after.getEventTicketing().getTicketingCloseAt());
        addChangeIfDifferent(changes, "ticketingLink",
                before.getEventTicketing() == null ? null : before.getEventTicketing().getTicketingLink(),
                after.getEventTicketing() == null ? null : after.getEventTicketing().getTicketingLink());

        return changes;
    }


    // 두 값이 다르면 FieldChange를 리스트에 추가
    private void addChangeIfDifferent(List<FieldChange> changes, String fieldName, Object before, Object after) {
        if (!equals(before, after)) {
            changes.add(new FieldChange(fieldName, before, after));
        }
    }

    // Null 안전 비교
    private boolean equals(Object before, Object after) {
        if (before == null && after == null) {
            return true;
        }
        if (before == null || after == null) {
            return false;
        }
        return before.equals(after);
    }

    // EventTime이 변경되었는지 확인
    private boolean hasEventTimeChanged() {
        if (before.getEventTime() == null || after.getEventTime() == null) {
            return before.getEventTime() != after.getEventTime();
        }
        return !before.getEventTime().equals(after.getEventTime());
    }


    // EventLocation이 변경되었는지 확인
    private boolean hasEventLocationChanged() {
        if (before.getEventLocation() == null || after.getEventLocation() == null) {
            return before.getEventLocation() != after.getEventLocation();
        }
        return !before.getEventLocation().equals(after.getEventLocation());
    }

    // EventFee가 변경되었는지 확인
    private boolean hasEventFeeChanged() {
        if (before.getEventFee() == null || after.getEventFee() == null) {
            return before.getEventFee() != after.getEventFee();
        }
        return !before.getEventFee().equals(after.getEventFee());
    }

    // EventTicketing이 변경되었는지 확인
    private boolean hasEventTicketingChanged() {
        if (before.getEventTicketing() == null || after.getEventTicketing() == null) {
            return before.getEventTicketing() != after.getEventTicketing();
        }
        return !before.getEventTicketing().equals(after.getEventTicketing());
    }

    // Getters
    public EventSnapshot getBefore() {
        return before;
    }

    public EventSnapshot getAfter() {
        return after;
    }

    public List<FieldChange> getChangedFields() {
        return changedFields;
    }


    // 실제로 변경된 필드가 있는지 확인
    public boolean hasChanges() {
        return !changedFields.isEmpty();
    }


    // 특정 필드가 변경되었는지 확인
    public boolean isFieldChanged(String fieldName) {
        return changedFields.stream()
                .anyMatch(change -> change.fieldName().equals(fieldName));
    }
}