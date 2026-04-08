package com.livent.event.adapter.outbound.persistence.repository

import com.livent.event.adapter.outbound.persistence.entity.ChatRoomEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface ChatRoomR2dbcRepository : ReactiveCrudRepository<ChatRoomEntity, Long> {
    fun findByEventIdOrderByTypeAsc(eventId: Long): Flux<ChatRoomEntity>
}
