package com.livent.message.adapter.outbound.persistence.entity

import java.time.Instant
import com.livent.message.domain.type.MessageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("messages")
data class MessageEntity(
    @Id
    val id: Long? = null,
    val chatRoomId: Long,
    val senderId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
)
