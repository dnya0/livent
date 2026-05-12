package com.livent.participation.adapter.inbound.web

import java.time.Instant
import com.livent.common.adapter.inbound.web.LiventValidationExceptionHandler
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.type.EventVisibility
import com.livent.event.domain.value.ChatRoomId
import com.livent.event.domain.value.EventDetails
import com.livent.event.domain.value.EventId
import com.livent.event.domain.value.EventLocation
import com.livent.event.domain.value.EventSchedule
import com.livent.event.domain.value.EventTimezone
import com.livent.event.domain.value.EventTitle
import com.livent.participation.application.ParticipationCommandService
import com.livent.participation.application.ParticipationQueryService
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ParticipationControllerTest {
    private lateinit var repository: FakeParticipationRepository
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        repository = FakeParticipationRepository()
        webTestClient = WebTestClient.bindToController(
            ParticipationController(
                participationCommandService = ParticipationCommandService(
                    participationRepository = repository,
                    eventRepository = FakeEventRepository(),
                    userRepository = FakeUserRepository(),
                ),
                participationQueryService = ParticipationQueryService(repository),
            ),
        )
            .controllerAdvice(LiventValidationExceptionHandler())
            .build()
    }

    @Test
    fun `post participation returns created participation`() {
        webTestClient.post()
            .uri("/events/1/participations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "userId": 2,
                  "status": "ONSITE"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.userId").isEqualTo(2)
            .jsonPath("$.data.eventId").isEqualTo(1)
            .jsonPath("$.data.status").isEqualTo("ONSITE")
    }

    @Test
    fun `get participation returns participation`() {
        webTestClient.get()
            .uri("/events/1/participations/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.userId").isEqualTo(1)
            .jsonPath("$.data.eventId").isEqualTo(1)
            .jsonPath("$.data.status").isEqualTo("ONLINE")
    }

    @Test
    fun `post participation validates request body`() {
        webTestClient.post()
            .uri("/events/1/participations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "status": "ONLINE"
                }
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    private class FakeParticipationRepository : ParticipationRepository {
        private var participation: Participation = Participation(
            id = ParticipationId.of(1L),
            userId = UserId.of(1L),
            eventId = EventId.of(1L),
            status = ParticipationStatus.ONLINE,
            joinedAt = Instant.parse("2026-05-12T00:00:00Z"),
        )

        override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> =
            participation.takeIf { it.eventId == eventId && it.userId == userId }?.let { Mono.just(it) } ?: Mono.empty()

        override fun save(participation: NewParticipation): Mono<Participation> {
            this.participation = participation.persist(ParticipationId.of(1L))
            return Mono.just(this.participation)
        }
    }

    private class FakeEventRepository : EventRepository {
        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> = Mono.just(existingEvent())

        override fun save(event: NewEvent): Mono<Event> = Mono.just(event.persist(EventId.of(1L)))

        override fun update(event: Event): Mono<Event> = Mono.just(event)

        override fun deleteById(id: EventId): Mono<Long> = Mono.just(1L)

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(id == EventId.of(1L))

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> = Flux.empty()

        override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> = Mono.just(
            chatRoom.persist(ChatRoomId.of(1L)),
        )

        private fun existingEvent(): Event = Event(
            id = EventId.of(1L),
            details = EventDetails(
                title = EventTitle.of("Seoul Tech Meetup"),
                location = EventLocation.of("COEX"),
                schedule = EventSchedule(
                    startTime = Instant.parse("2026-04-20T10:00:00Z"),
                    endTime = Instant.parse("2026-04-20T12:00:00Z"),
                    timezone = EventTimezone.of("Asia/Seoul"),
                ),
                visibility = EventVisibility.BOTH,
            ),
        )
    }

    private class FakeUserRepository : UserRepository {
        override fun findById(id: UserId): Mono<User> = Mono.just(
            User(
                id = UserId.of(1L),
                nickname = UserNickname.of("ahnnayeong"),
            ),
        )

        override fun existsById(id: UserId): Mono<Boolean> = Mono.just(id == UserId.of(1L) || id == UserId.of(2L))

        override fun save(user: NewUser): Mono<User> = Mono.just(user.persist(UserId.of(1L)))
    }
}
