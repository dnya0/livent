package com.livent.event.domain.value

@JvmInline
value class ChatRoomName private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): ChatRoomName {
            val normalized = raw.trim()
            require(normalized.isNotEmpty()) { "채팅방 이름은 비어 있을 수 없습니다." }
            require(normalized.length <= 255) { "채팅방 이름은 255자를 초과할 수 없습니다." }
            return ChatRoomName(normalized)
        }
    }
}
