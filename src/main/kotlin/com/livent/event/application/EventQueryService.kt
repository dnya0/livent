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
    fun getEventSlice(cursor: String?, size: Int): Mono<EventSlice<Event>> {
        require(size > 0) { "size must be greater than 0." }

        val boundedSize = size.coerceAtMost(MAX_PAGE_SIZE)
        val fetchLimit = boundedSize + 1
        val cursorId = decodeCursor(cursor)

        val events = if (cursorId == null) {
            eventRepository.findFirstPage(limit = fetchLimit)
        } else {
            eventRepository.findAfterId(cursor = cursorId, limit = fetchLimit)
        }

        return events.collectList()
            .map { results ->
                val hasNext = results.size > boundedSize
                val items = if (hasNext) results.take(boundedSize) else results
                val nextCursor = if (hasNext) items.lastOrNull()?.id?.toString() else null

                EventSlice(items = items, size = items.size, hasNext = hasNext, nextCursor = nextCursor)
            }
    }

    fun getEvent(eventId: Long): Mono<Event> =
        eventRepository.findById(eventId)
            .switchIfEmpty(Mono.error(EventNotFoundException()))

    fun getChatRooms(eventId: Long): Flux<ChatRoom> =
        getEvent(eventId)
            .flatMapMany { chatRoomRepository.findByEventId(eventId) }

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 20
        const val MAX_PAGE_SIZE: Int = 100
    }

    private fun decodeCursor(cursor: String?): Long? {
        if (cursor == null) return null

        return cursor.toLongOrNull()
            ?.takeIf { it >= 0 }
            ?: throw IllegalArgumentException("cursor must be a non-negative number.")
    }
}

data class EventSlice<T>(
    val items: List<T>,
    val size: Int,
    val hasNext: Boolean,
    val nextCursor: String?,
)
