package com.livent.event.domain.repository

import com.livent.event.domain.model.ChatRoom
import reactor.core.publisher.Flux

interface ChatRoomRepository {
    fun findByEventId(eventId: Long): Flux<ChatRoom>
}
