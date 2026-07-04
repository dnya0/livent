package com.livent.message.domain.exception

import com.project.common.core.domain.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class MessageErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus,
    override val group: String,
) : ErrorCode {
    MESSAGE_NOT_FOUND(40431, "메시지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "client"),
}
