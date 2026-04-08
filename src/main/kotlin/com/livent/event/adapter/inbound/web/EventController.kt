package com.livent.event.adapter.inbound.web

import com.livent.common.adapter.inbound.web.ApiResponse
import com.livent.common.adapter.inbound.web.CursorApiResponse
import com.livent.common.adapter.inbound.web.CursorPageInfo
import com.livent.event.application.EventQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/events")
class EventController(
    private val eventQueryService: EventQueryService,
) {
    @GetMapping
    fun getEvents(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") size: Int,
    ): Mono<CursorApiResponse<EventResponse>> =
        eventQueryService.getEventSlice(cursor = cursor, size = size)
            .map { slice ->
                CursorApiResponse(
                    data = slice.items.map { it.toResponse() },
                    pageInfo = CursorPageInfo(
                        size = slice.size,
                        hasNext = slice.hasNext,
                        nextCursor = slice.nextCursor,
                    ),
                )
            }

    @GetMapping("/{eventId}")
    fun getEvent(
        @PathVariable eventId: Long,
    ): Mono<ApiResponse<EventResponse>> =
        eventQueryService.getEvent(eventId)
            .map { ApiResponse.ok(it.toResponse()) }

    @GetMapping("/{eventId}/chat-rooms")
    fun getChatRooms(
        @PathVariable eventId: Long,
    ): Mono<ApiResponse<List<ChatRoomResponse>>> =
        eventQueryService.getChatRooms(eventId)
            .map { it.toResponse() }
            .collectList()
            .map { ApiResponse.ok(it) }
}
