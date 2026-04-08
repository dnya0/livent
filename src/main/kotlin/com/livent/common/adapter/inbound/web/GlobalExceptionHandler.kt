package com.livent.common.adapter.inbound.web

import com.livent.common.exception.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException::class)
    fun handleEventNotFound(ex: EventNotFoundException) =
        errorResponse(ApiErrorCode.EVENT_NOT_FOUND)

    @ExceptionHandler(ChatRoomNotFoundException::class)
    fun handleChatRoomNotFound(ex: ChatRoomNotFoundException) =
        errorResponse(ApiErrorCode.CHAT_ROOM_NOT_FOUND)

    @ExceptionHandler(ChatRoomAccessDeniedException::class)
    fun handleChatRoomAccessDenied(ex: ChatRoomAccessDeniedException) =
        errorResponse(ApiErrorCode.CHAT_ROOM_ACCESS_DENIED)

    @ExceptionHandler(MessageNotFoundException::class)
    fun handleMessageNotFound(ex: MessageNotFoundException) =
        errorResponse(ApiErrorCode.MESSAGE_NOT_FOUND)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException) =
        errorResponse(ApiErrorCode.USER_NOT_FOUND)

    @ExceptionHandler(AlreadyParticipatingException::class)
    fun handleAlreadyParticipating(ex: AlreadyParticipatingException) =
        errorResponse(ApiErrorCode.ALREADY_PARTICIPATING)

    @ExceptionHandler(ParticipationNotFoundException::class)
    fun handleParticipationNotFound(ex: ParticipationNotFoundException) =
        errorResponse(ApiErrorCode.PARTICIPATION_NOT_FOUND)

    @ExceptionHandler(LocationAccessDeniedException::class)
    fun handleLocationAccessDenied(ex: LocationAccessDeniedException) =
        errorResponse(ApiErrorCode.LOCATION_ACCESS_DENIED)

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException) =
        errorResponse(ApiErrorCode.INVALID_INPUT)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException) =
        errorResponse(ApiErrorCode.INVALID_INPUT)

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception) =
        errorResponse(ApiErrorCode.INTERNAL_SERVER_ERROR)

    private fun errorResponse(errorCode: ApiErrorCode): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(errorCode.status).body(ApiResponse.fail(errorCode))
}
