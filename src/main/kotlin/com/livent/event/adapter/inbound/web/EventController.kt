package com.livent.event.adapter.inbound.web

import com.livent.event.application.EventQueryService
import com.livent.event.application.EventSlice
import com.livent.event.domain.model.Event
import com.project.common.core.presentation.response.ApiResponse
import com.project.common.core.presentation.response.CursorApiResponse
import com.project.common.core.presentation.response.CursorPageInfo
import com.project.common.core.presentation.response.responseOf
import com.project.webflux.presentation.request.CursorRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/events")
class EventController(
    private val eventQueryService: EventQueryService,
) {
    @GetMapping
    fun getEvents(@Valid @ModelAttribute cursorRequest: CursorRequest): Mono<CursorApiResponse<EventResponse>> =
        eventQueryService.getEventSlice(cursor = cursorRequest.cursor, size = cursorRequest.size)
            .map(::toCursorResponse)

    @GetMapping("/{eventId}")
    fun getEvent(@PathVariable eventId: Long): Mono<ApiResponse<EventResponse>> =
        eventQueryService.getEvent(eventId).map { responseOf(it.toResponse()) }

    @GetMapping("/{eventId}/chat-rooms")
    fun getChatRooms(@PathVariable eventId: Long): Mono<ApiResponse<List<ChatRoomResponse>>> =
        eventQueryService.getChatRooms(eventId)
            .map { it.toResponse() }
            .collectList()
            .map { responseOf(it) }

    private fun toCursorResponse(slice: EventSlice<Event>): CursorApiResponse<EventResponse> = CursorApiResponse(
        data = slice.items.map { it.toResponse() },
        pageInfo = CursorPageInfo(
            size = slice.size,
            hasNext = slice.hasNext,
            nextCursor = slice.nextCursor,
        ),
    )
}
