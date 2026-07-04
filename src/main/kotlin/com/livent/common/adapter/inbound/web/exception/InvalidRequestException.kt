package com.livent.common.adapter.inbound.web.exception

class InvalidRequestException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

inline fun <T> invalidRequestCatch(
    message: String,
    useCauseMessage: Boolean = false,
    block: () -> T,
): T = try {
    block()
} catch (ex: InvalidRequestException) {
    throw ex
} catch (ex: IllegalArgumentException) {
    throw InvalidRequestException(if (useCauseMessage) ex.message ?: message else message, ex)
}
