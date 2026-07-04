package com.livent.event.adapter.outbound.persistence.repository

import com.livent.event.adapter.outbound.persistence.entity.ChatRoomEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChatRoomR2dbcRepository : ReactiveCrudRepository<ChatRoomEntity, Long> {
    override fun findById(id: Long): Mono<ChatRoomEntity>

    fun findByEventIdOrderByTypeAsc(eventId: Long): Flux<ChatRoomEntity>
}
