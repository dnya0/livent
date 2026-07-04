package com.livent.message.domain.value

@JvmInline
value class MessageContent private constructor(
    val value: String,
) {
    companion object {
        private const val MAX_LENGTH = 1000

        fun of(raw: String): MessageContent {
            val trimmed = raw.trim()
            require(trimmed.isNotBlank()) { "메시지 내용은 비어 있을 수 없습니다." }
            require(trimmed.length <= MAX_LENGTH) { "메시지 내용은 ${MAX_LENGTH}자 이하여야 합니다." }
            return MessageContent(trimmed)
        }
    }
}
