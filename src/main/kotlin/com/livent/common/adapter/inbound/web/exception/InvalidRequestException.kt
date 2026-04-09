package com.livent.common.adapter.inbound.web.exception

class InvalidRequestException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
