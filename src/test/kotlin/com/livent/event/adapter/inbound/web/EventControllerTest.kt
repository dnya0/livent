package com.livent.event.adapter.inbound.web

import java.time.Instant
import com.livent.common.adapter.inbound.web.LiventValidationExceptionHandler
import com.livent.event.application.EventCommandService
import com.livent.event.application.EventQueryService
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.type.EventVisibility
import com.livent.event.domain.model.value.ChatRoomId
import com.livent.event.domain.model.value.ChatRoomName
import com.livent.event.domain.model.value.EventDetails
import com.livent.event.domain.model.value.EventId
import com.livent.event.domain.model.value.EventTimezone
import com.livent.event.domain.repository.EventRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class EventControllerTest {
    private lateinit var repository: FakeEventRepository
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        repository = FakeEventRepository()
        webTestClient = WebTestClient.bindToController(
            EventController(
                eventCommandService = EventCommandService(repository),
                eventQueryService = EventQueryService(repository),
            ),
        )
            .controllerAdvice(LiventValidationExceptionHandler())
            .build()
    }

    @Test
    fun `post events returns created event`() {
        webTestClient.post()
            .uri("/events")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "title": "Seoul Tech Meetup",
                  "location": "COEX",
                  "startTime": "2026-04-20T10:00:00Z",
                  "endTime": "2026-04-20T12:00:00Z",
                  "timezone": "Asia/Seoul",
                  "visibility": "BOTH"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.title").isEqualTo("Seoul Tech Meetup")
            .jsonPath("$.data.timezone").isEqualTo("Asia/Seoul")
    }

    @Test
    fun `patch events returns updated event`() {
        webTestClient.patch()
            .uri("/events/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "title": "Platform Meetup",
                  "location": "DDP",
                  "startTime": "2026-04-21T10:00:00Z",
                  "endTime": "2026-04-21T13:00:00Z",
                  "timezone": "Asia/Tokyo",
                  "visibility": "ONSITE"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.title").isEqualTo("Platform Meetup")
            .jsonPath("$.data.location").isEqualTo("DDP")
            .jsonPath("$.data.timezone").isEqualTo("Asia/Tokyo")
            .jsonPath("$.data.visibility").isEqualTo("ONSITE")
    }

    @Test
    fun `delete events returns ok`() {
        webTestClient.delete()
            .uri("/events/1")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `patch events validates request body`() {
        webTestClient.patch()
            .uri("/events/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "title": " ",
                  "location": "DDP",
                  "startTime": "2026-04-21T10:00:00Z",
                  "endTime": "2026-04-21T13:00:00Z",
                  "timezone": "Asia/Tokyo",
                  "visibility": "ONSITE"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    private class FakeEventRepository : EventRepository {
        private var event: Event = Event(
            id = EventId.of(1L),
            details = EventDetails.create(
                title = "Seoul Tech Meetup",
                location = "COEX",
                startTime = Instant.parse("2026-04-20T10:00:00Z"),
                endTime = Instant.parse("2026-04-20T12:00:00Z"),
                timezone = EventTimezone.of("Asia/Seoul"),
                visibility = EventVisibility.BOTH,
            ),
        )

        override fun findFirstPage(limit: Int): Flux<Event> = Flux.just(event).take(limit.toLong())

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> =
            if (event.id.value > cursor.value) Flux.just(event).take(limit.toLong()) else Flux.empty()

        override fun findById(id: EventId): Mono<Event> =
            if (event.id == id) Mono.just(event) else Mono.empty()

        override fun save(event: NewEvent): Mono<Event> {
            this.event = event.persist(EventId.of(1L))
            return Mono.just(this.event)
        }

        override fun update(event: Event): Mono<Event> {
            this.event = event
            return Mono.just(this.event)
        }

        override fun deleteById(id: EventId): Mono<Void> {
            if (event.id == id) {
                event = Event(
                    id = EventId.of(999L),
                    details = EventDetails.create(
                        title = "Deleted",
                        location = "N/A",
                        startTime = Instant.parse("2026-04-22T10:00:00Z"),
                        endTime = Instant.parse("2026-04-22T11:00:00Z"),
                        timezone = EventTimezone.of("Asia/Seoul"),
                        visibility = EventVisibility.ONSITE,
                    ),
                )
            }
            return Mono.empty()
        }

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(event.id == id)

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> =
            if (event.id != eventId) Flux.empty()
            else Flux.just(
                ChatRoom(
                    id = ChatRoomId.of(1L),
                    eventId = eventId,
                    type = ChatRoomType.GLOBAL,
                    name = ChatRoomName.of("전체 채팅"),
                ),
            )
    }
}
