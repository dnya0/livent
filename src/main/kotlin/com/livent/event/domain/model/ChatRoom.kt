package com.livent.event.domain.model

import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.value.ChatRoomId
import com.livent.event.domain.model.value.ChatRoomName
import com.livent.event.domain.model.value.EventId

data class ChatRoom(
    val id: ChatRoomId,
    val eventId: EventId,
    val type: ChatRoomType,
    val name: ChatRoomName,
)
