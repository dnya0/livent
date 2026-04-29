package com.livent.event.domain.exception

import com.project.common.core.domain.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class EventErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus,
    override val group: String,
) : ErrorCode {
    EVENT_NOT_FOUND(40401, "이벤트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "client"),
    CHAT_ROOM_NOT_FOUND(40402, "채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "client"),
    CHAT_ROOM_ACCESS_DENIED(40301, "채팅방에 접근할 수 없습니다.", HttpStatus.FORBIDDEN, "client"),
    CHAT_ROOM_ALREADY_EXISTS(40901, "같은 유형의 채팅방이 이미 존재합니다.", HttpStatus.CONFLICT, "client"),
}
