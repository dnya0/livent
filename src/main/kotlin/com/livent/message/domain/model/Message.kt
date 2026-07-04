package com.livent.message.domain.model

import java.time.Instant
import com.livent.event.domain.value.ChatRoomId
import com.livent.message.domain.type.MessageType
import com.livent.message.domain.value.MessageContent
import com.livent.message.domain.value.MessageId
import com.livent.user.domain.value.UserId

data class Message(
    val id: MessageId,
    val chatRoomId: ChatRoomId,
    val senderId: UserId,
    val content: MessageContent,
    val type: MessageType,
    val createdAt: Instant,
)

data class NewMessage(
    val chatRoomId: ChatRoomId,
    val senderId: UserId,
    val content: MessageContent,
    val type: MessageType,
    val createdAt: Instant,
) {
    fun persist(id: MessageId): Message = Message(
        id = id,
        chatRoomId = chatRoomId,
        senderId = senderId,
        content = content,
        type = type,
        createdAt = createdAt,
    )

    companion object {
        fun text(
            chatRoomId: ChatRoomId,
            senderId: UserId,
            content: String,
            createdAt: Instant = Instant.now(),
        ): NewMessage = NewMessage(
            chatRoomId = chatRoomId,
            senderId = senderId,
            content = MessageContent.of(content),
            type = MessageType.TEXT,
            createdAt = createdAt,
        )
    }
}
