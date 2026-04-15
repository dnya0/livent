package com.livent.event.domain.repository

import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.model.value.EventId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventRepository {
    fun findFirstPage(limit: Int): Flux<Event>

    fun findAfterId(cursor: EventId, limit: Int): Flux<Event>

    fun findById(id: EventId): Mono<Event>

    fun save(event: NewEvent): Mono<Event>

    fun update(event: Event): Mono<Event>

    fun deleteById(id: EventId): Mono<Long>

    fun existsById(id: EventId): Mono<Boolean>

    fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom>
}
