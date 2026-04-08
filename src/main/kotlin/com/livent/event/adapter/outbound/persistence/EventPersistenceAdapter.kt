package com.livent.event.adapter.outbound.persistence

import com.livent.event.adapter.outbound.persistence.entity.ChatRoomEntity
import com.livent.event.adapter.outbound.persistence.entity.EventEntity
import com.livent.event.adapter.outbound.persistence.repository.ChatRoomR2dbcRepository
import com.livent.event.adapter.outbound.persistence.repository.EventR2dbcRepository
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.repository.ChatRoomRepository
import com.livent.event.domain.repository.EventRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class EventPersistenceAdapter(
    private val eventR2dbcRepository: EventR2dbcRepository,
    private val chatRoomR2dbcRepository: ChatRoomR2dbcRepository,
) : EventRepository, ChatRoomRepository {

    override fun findFirstPage(limit: Int): Flux<Event> =
        eventR2dbcRepository.findFirstPage(limit = limit)
            .map(EventEntity::toDomain)

    override fun findAfterId(cursor: Long, limit: Int): Flux<Event> =
        eventR2dbcRepository.findAfterId(cursor = cursor, limit = limit)
            .map(EventEntity::toDomain)

    override fun findById(id: Long): Mono<Event> = eventR2dbcRepository.findById(id)
        .map(EventEntity::toDomain)

    override fun existsById(id: Long): Mono<Boolean> = eventR2dbcRepository.existsById(id)

    override fun findByEventId(eventId: Long): Flux<ChatRoom> =
        chatRoomR2dbcRepository.findByEventIdOrderByTypeAsc(eventId).map(ChatRoomEntity::toDomain)
}

private fun EventEntity.toDomain(): Event = Event(
    id = requireNotNull(id) { "Event id must not be null when reading." },
    title = title,
    location = location,
    startTime = startTime,
    endTime = endTime,
    visibility = visibility,
)

private fun ChatRoomEntity.toDomain(): ChatRoom = ChatRoom(
    id = requireNotNull(id) { "ChatRoom id must not be null when reading." },
    eventId = eventId,
    type = type,
    name = name,
)
