package com.livent.event.application

import com.livent.common.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.repository.ChatRoomRepository
import com.livent.event.domain.model.Event
import com.livent.event.domain.repository.EventRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class EventQueryService(
    private val eventRepository: EventRepository,
    private val chatRoomRepository: ChatRoomRepository,
) {
    fun getEvents(): Flux<Event> = eventRepository.findAll()

    fun getEvent(eventId: Long): Mono<Event> =
        eventRepository.findById(eventId)
            .switchIfEmpty(Mono.error(EventNotFoundException()))

    fun getChatRooms(eventId: Long): Flux<ChatRoom> =
        getEvent(eventId)
            .flatMapMany { chatRoomRepository.findByEventId(eventId) }
}
