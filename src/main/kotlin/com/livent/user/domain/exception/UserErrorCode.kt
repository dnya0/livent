package com.livent.user.domain.exception

import com.project.common.core.domain.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus,
    override val group: String,
) : ErrorCode {
    USER_NOT_FOUND(40411, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "client"),
}
