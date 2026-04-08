package com.livent.event.adapter.outbound.persistence.entity

import com.livent.event.domain.model.type.ChatRoomType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("chat_rooms")
data class ChatRoomEntity(
    @Id
    val id: Long? = null,
    val eventId: Long,
    val type: ChatRoomType,
    val name: String,
)
