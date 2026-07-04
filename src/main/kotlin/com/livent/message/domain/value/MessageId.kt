package com.livent.message.domain.value

@JvmInline
value class MessageId private constructor(
    val value: Long,
) {
    companion object {
        fun of(raw: Long): MessageId {
            require(raw > 0) { "메시지 식별자는 0보다 커야 합니다." }
            return MessageId(raw)
        }
    }
}
