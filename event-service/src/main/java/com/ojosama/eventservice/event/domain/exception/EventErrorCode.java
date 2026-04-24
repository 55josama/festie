package com.ojosama.eventservice.event.domain.exception;

import com.ojosama.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {
    // Event 관련 에러
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다."),
    EVENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이벤트입니다."),
    EVENT_INVALID_TIME(HttpStatus.BAD_REQUEST, "이벤트 시간이 유효하지 않습니다. 시작 시간이 종료 시간보다 작아야 합니다."),
    EVENT_PAST_START_TIME(HttpStatus.BAD_REQUEST, "시작 시간은 현재 시간 이후여야 합니다."),
    EVENT_INVALID_NAME(HttpStatus.BAD_REQUEST, "이벤트 이름이 유효하지 않습니다."),
    EVENT_INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST, "이벤트 설명이 유효하지 않습니다."),
    EVENT_INVALID_LOCATION(HttpStatus.BAD_REQUEST, "이벤트 위치 정보가 유효하지 않습니다."),
    EVENT_INVALID_FEE(HttpStatus.BAD_REQUEST, "이벤트 수수료가 유효하지 않습니다. 최소 수수료가 최대 수수료보다 클 수 없습니다."),
    EVENT_INVALID_IMAGE(HttpStatus.BAD_REQUEST, "이벤트 이미지가 유효하지 않습니다."),

    // EventCategory 관련 에러
    EVENT_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),
    EVENT_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "이벤트 카테고리가 유효하지 않습니다."),

    // EventSchedule 관련 에러
    EVENT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 스케줄을 찾을 수 없습니다."),
    EVENT_SCHEDULE_INVALID_TIME(HttpStatus.BAD_REQUEST, "스케줄 시간이 유효하지 않습니다."),
    EVENT_SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 다른 스케줄이 존재합니다."),

    // Ticketing 관련 에러
    TICKETING_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "현재 티켓팅이 불가능합니다."),
    TICKETING_CLOSED(HttpStatus.BAD_REQUEST, "티켓팅 기간이 종료되었습니다."),
    TICKETING_NOT_OPENED(HttpStatus.BAD_REQUEST, "티켓팅이 아직 시작되지 않았습니다."),
    TICKETING_INVALID_TIME(HttpStatus.BAD_REQUEST, "티켓팅 시간이 유효하지 않습니다."),

    // 위치 관련 에러
    INVALID_LATITUDE(HttpStatus.BAD_REQUEST, "위도는 -90 ~ 90 범위여야 합니다."),
    INVALID_LONGITUDE(HttpStatus.BAD_REQUEST, "경도는 -180 ~ 180 범위여야 합니다."),

    // 권한 관련 에러
    EVENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "이 이벤트에 대한 권한이 없습니다."),
    EVENT_INVALID_STATE(HttpStatus.CONFLICT, "이벤트 상태가 유효하지 않습니다."),
    EVENT_CANNOT_DELETE(HttpStatus.CONFLICT, "이 상태의 이벤트는 삭제할 수 없습니다."),
    EVENT_CANNOT_UPDATE(HttpStatus.CONFLICT, "이 상태의 이벤트는 수정할 수 없습니다."),

    // 공통 에러
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력 값이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}