package com.livent.participation.domain.exception

import com.project.common.core.domain.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ParticipationErrorCode(
    override val code: Int,
    override val message: String,
    override val httpStatus: HttpStatus,
    override val group: String,
) : ErrorCode {
    PARTICIPATION_NOT_FOUND(40421, "참여 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "client"),
    PARTICIPATION_ALREADY_EXISTS(40921, "이미 참여한 이벤트입니다.", HttpStatus.CONFLICT, "client"),
}
