package com.livent.user.domain.value

@JvmInline
value class UserNickname private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): UserNickname {
            val normalized = raw.trim()
            require(normalized.isNotEmpty()) { "닉네임은 비어 있을 수 없습니다." }
            require(normalized.length <= 255) { "닉네임은 255자를 초과할 수 없습니다." }
            return UserNickname(normalized)
        }
    }
}
