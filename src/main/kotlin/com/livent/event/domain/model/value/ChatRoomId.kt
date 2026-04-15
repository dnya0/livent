package com.livent.event.domain.model.value

@JvmInline
value class ChatRoomId private constructor(
    val value: Long,
) {
    companion object {
        fun of(raw: Long): ChatRoomId {
            require(raw > 0) { "채팅방 식별자는 0보다 커야 합니다." }
            return ChatRoomId(raw)
        }
    }
}
