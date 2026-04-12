package com.livent.event.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.repository.EventRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class EventQueryService(
    private val eventRepository: EventRepository,
) {
    fun getEventSlice(cursor: String?, size: Int): Mono<EventSlice<Event>> {
        val request = resolveSliceRequest(cursor = cursor, size = size)

        return requestEvents(request)
            .collectList()
            .map { toEventSlice(results = it, pageSize = request.pageSize) }
    }

    fun getEvent(eventId: Long): Mono<Event> = eventRepository.findById(eventId)
        .switchIfEmpty(Mono.error(EventNotFoundException()))

    fun getChatRooms(eventId: Long): Flux<ChatRoom> = eventRepository.findChatRoomsByEventId(eventId)
        .collectList()
        .flatMapMany { resolveChatRooms(eventId = eventId, chatRooms = it) }

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 20
        const val MAX_PAGE_SIZE: Int = 100
    }

    private fun resolveSliceRequest(cursor: String?, size: Int): EventSliceRequest {
        if (size <= 0) {
            throw InvalidRequestException("size must be greater than 0.")
        }

        val pageSize = size.coerceAtMost(MAX_PAGE_SIZE)
        return EventSliceRequest(
            cursorId = decodeCursor(cursor),
            pageSize = pageSize,
            fetchLimit = pageSize + 1,
        )
    }

    private fun requestEvents(request: EventSliceRequest): Flux<Event> =
        if (request.cursorId == null) {
            eventRepository.findFirstPage(limit = request.fetchLimit)
        } else {
            eventRepository.findAfterId(cursor = request.cursorId, limit = request.fetchLimit)
        }

    private fun toEventSlice(results: List<Event>, pageSize: Int): EventSlice<Event> {
        val hasNext = results.size > pageSize
        val items = if (hasNext) results.take(pageSize) else results
        val nextCursor = if (hasNext) items.lastOrNull()?.id?.toString() else null

        return EventSlice(
            items = items,
            size = items.size,
            hasNext = hasNext,
            nextCursor = nextCursor,
        )
    }

    private fun resolveChatRooms(eventId: Long, chatRooms: List<ChatRoom>): Flux<ChatRoom> =
        if (chatRooms.isNotEmpty()) {
            Flux.fromIterable(chatRooms)
        } else {
            eventRepository.existsById(eventId)
                .flatMapMany { exists ->
                    if (exists) Flux.empty()
                    else Flux.error(EventNotFoundException())
                }
        }

    private fun decodeCursor(cursor: String?): Long? {
        if (cursor == null) return null

        return cursor.toLongOrNull()
            ?.takeIf { it >= 0 }
            ?: throw InvalidRequestException("cursor must be a non-negative number.")
    }
}

private data class EventSliceRequest(
    val cursorId: Long?,
    val pageSize: Int,
    val fetchLimit: Int,
)

data class EventSlice<T>(
    val items: List<T>,
    val size: Int,
    val hasNext: Boolean,
    val nextCursor: String?,
)
