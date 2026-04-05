package com.livent.common.adapter.inbound.web

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> ok(data: T? = null, message: String = "요청이 성공했습니다."): ApiResponse<T> =
            ApiResponse(success = true, code = "SUCCESS", message = message, data = data)

        fun <T> fail(errorCode: ApiErrorCode): ApiResponse<T> =
            ApiResponse(success = false, code = errorCode.code, message = errorCode.message)
    }
}
