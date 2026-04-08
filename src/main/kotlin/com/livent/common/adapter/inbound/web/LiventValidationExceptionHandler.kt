package com.livent.common.adapter.inbound.web

import com.project.common.core.domain.exception.CommonErrorCode
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.errorOf
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
class LiventValidationExceptionHandler {

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): ResponseEntity<ApiResponse<Nothing>> = ResponseEntity
        .badRequest()
        .body(errorOf(CommonErrorCode.INVALID_REQUEST, ex.allErrors.firstOrNull()?.defaultMessage))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> = ResponseEntity
        .badRequest()
        .body(errorOf(CommonErrorCode.INVALID_REQUEST, ex.message))
}
