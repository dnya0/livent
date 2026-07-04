package com.livent.common.adapter.inbound.web.exception

class InvalidRequestException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

inline fun <T> invalidRequestCatch(message: String, block: () -> T): T = try {
    block()
} catch (ex: InvalidRequestException) {
    throw ex
} catch (ex: IllegalArgumentException) {
    throw InvalidRequestException(message, ex)
}
