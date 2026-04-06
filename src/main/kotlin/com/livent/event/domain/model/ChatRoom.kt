package com.livent.event.domain.model

import com.livent.event.domain.model.type.ChatRoomType

data class ChatRoom(
    val id: Long,
    val eventId: Long,
    val type: ChatRoomType,
    val name: String,
)
