package com.livent.user.adapter.inbound.web

import jakarta.validation.constraints.NotBlank

data class UserPayload(
    @field:NotBlank(message = "nickname must not be blank.")
    val nickname: String,
)
