package com.livent.event.adapter.inbound.web

import java.time.Instant
import com.livent.common.adapter.inbound.web.LiventValidationExceptionHandler
import com.livent.event.application.EventCommandService
import com.livent.event.application.EventQueryService
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewChatRoom
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

        webTestClient.get()
            .uri("/events/1/chat-rooms")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data[0].type").isEqualTo("GLOBAL")
            .jsonPath("$.data[0].name").isEqualTo("전체 채팅")
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
            .expectBody()
            .jsonPath("$.data.deletedCount").isEqualTo(1)
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

    @Test
    fun `post event chat rooms returns created room`() {
        webTestClient.post()
            .uri("/events/1/chat-rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "type": "LOCAL",
                  "name": "현장 참가자"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.eventId").isEqualTo(1)
            .jsonPath("$.data.type").isEqualTo("LOCAL")
            .jsonPath("$.data.name").isEqualTo("현장 참가자")
    }

    @Test
    fun `post event chat rooms rejects duplicate type`() {
        webTestClient.post()
            .uri("/events/1/chat-rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "type": "GLOBAL"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    private class FakeEventRepository : EventRepository {
        private var event: Event? = Event(
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
        private val chatRooms = mutableListOf(
            ChatRoom(
                id = ChatRoomId.of(1L),
                eventId = EventId.of(1L),
                type = ChatRoomType.GLOBAL,
                name = ChatRoomName.of("전체 채팅"),
            ),
        )
        private var nextChatRoomId = 2L

        override fun findFirstPage(limit: Int): Flux<Event> =
            event?.let { Flux.just(it).take(limit.toLong()) } ?: Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> =
            event?.takeIf { it.id.value > cursor.value }
                ?.let { Flux.just(it).take(limit.toLong()) }
                ?: Flux.empty()

        override fun findById(id: EventId): Mono<Event> =
            event?.takeIf { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()

        override fun save(event: NewEvent): Mono<Event> {
            this.event = event.persist(EventId.of(1L))
            if (chatRooms.none { it.eventId == EventId.of(1L) && it.type == ChatRoomType.GLOBAL }) {
                chatRooms += ChatRoom(
                    id = ChatRoomId.of(nextChatRoomId++),
                    eventId = EventId.of(1L),
                    type = ChatRoomType.GLOBAL,
                    name = ChatRoomName.of("전체 채팅"),
                )
            }
            return Mono.just(requireNotNull(this.event))
        }

        override fun update(event: Event): Mono<Event> {
            this.event = event
            return Mono.just(requireNotNull(this.event))
        }

        override fun deleteById(id: EventId): Mono<Long> {
            if (event?.id == id) {
                event = null
                chatRooms.removeIf { it.eventId == id }
            }
            return Mono.just(1L)
        }

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(event?.id == id)

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> =
            if (event?.id != eventId) Flux.empty()
            else Flux.fromIterable(chatRooms.filter { it.eventId == eventId })

        override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> {
            val persisted = chatRoom.persist(ChatRoomId.of(nextChatRoomId++))
            chatRooms += persisted
            return Mono.just(persisted)
        }
    }
}
