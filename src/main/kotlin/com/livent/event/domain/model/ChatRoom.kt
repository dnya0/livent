package com.livent.event.domain.model

import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.value.ChatRoomId
import com.livent.event.domain.value.ChatRoomName
import com.livent.event.domain.value.EventId

data class ChatRoom(
    val id: ChatRoomId,
    val eventId: EventId,
    val type: ChatRoomType,
    val name: ChatRoomName,
)

data class NewChatRoom(
    val eventId: EventId,
    val type: ChatRoomType,
    val name: ChatRoomName,
) {
    fun persist(id: ChatRoomId): ChatRoom = ChatRoom(
        id = id,
        eventId = eventId,
        type = type,
        name = name,
    )

    companion object {
        fun create(
            eventId: EventId,
            type: ChatRoomType,
            name: String? = null,
        ): NewChatRoom = NewChatRoom(
            eventId = eventId,
            type = type,
            name = ChatRoomName.of(name?.takeIf { it.isNotBlank() } ?: type.defaultName),
        )
    }
}

private val ChatRoomType.defaultName: String
    get() = when (this) {
        ChatRoomType.GLOBAL -> "전체 채팅"
        ChatRoomType.LOCAL -> "현장 채팅"
        ChatRoomType.SESSION -> "세션 채팅"
    }
