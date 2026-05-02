package com.livent.user.domain.value

@JvmInline
value class UserId private constructor(
    val value: Long,
) {
    companion object {
        fun of(raw: Long): UserId {
            require(raw > 0) { "사용자 ID는 0보다 커야 합니다." }
            return UserId(raw)
        }
    }
}
