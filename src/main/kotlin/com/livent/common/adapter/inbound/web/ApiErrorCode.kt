package com.livent.common.adapter.inbound.web

import org.springframework.http.HttpStatus

enum class ApiErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "이벤트를 찾을 수 없습니다."),

    // ChatRoom
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_ROOM_ACCESS_DENIED", "채팅방에 접근할 수 없습니다."),

    // Message
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "메시지를 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // Participation
    ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "ALREADY_PARTICIPATING", "이미 참여 중인 이벤트입니다."),
    PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPATION_NOT_FOUND", "참여 정보를 찾을 수 없습니다."),

    // Location
    LOCATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "LOCATION_ACCESS_DENIED", "현장 참여자만 접근 가능한 채팅방입니다."),
}
