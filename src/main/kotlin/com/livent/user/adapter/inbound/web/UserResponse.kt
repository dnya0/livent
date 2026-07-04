package com.livent.user.adapter.inbound.web

import java.util.UUID
import com.livent.user.domain.model.User

data class UserResponse(
    val id: UUID,
    val nickname: String,
)

fun User.toResponse(): UserResponse = UserResponse(
    id = id.value,
    nickname = nickname.value,
)
