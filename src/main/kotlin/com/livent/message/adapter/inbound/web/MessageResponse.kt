package com.livent.message.adapter.inbound.web

import java.time.Instant
import java.util.UUID
import com.livent.message.domain.model.Message
import com.livent.message.domain.type.MessageType

data class MessageResponse(
    val id: Long,
    val chatRoomId: Long,
    val senderId: UUID,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
)

fun Message.toResponse(): MessageResponse = MessageResponse(
    id = id.value,
    chatRoomId = chatRoomId.value,
    senderId = senderId.value,
    content = content.value,
    type = type,
    createdAt = createdAt,
)
