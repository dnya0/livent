package com.livent.user.domain.value

import java.util.UUID

@JvmInline
value class UserId private constructor(
    val value: UUID,
) {
    companion object {
        fun new(): UserId = UserId(UUID.randomUUID())

        fun of(raw: UUID): UserId = UserId(raw)
    }
}
