package com.livent.participation.application

import java.time.Instant
import kotlin.test.assertEquals
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
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
import com.livent.participation.domain.exception.ParticipationAlreadyExistsException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.exception.UserNotFoundException
import com.livent.user.domain.model.NewUser
import com.livent.user.domain.model.User
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import com.livent.user.domain.value.UserNickname
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ParticipationCommandServiceTest {
    @Test
    fun `createParticipation saves online participation by default`() {
        val repository = FakeParticipationRepository()
        val service = ParticipationCommandService(
            participationRepository = repository,
            eventRepository = FakeEventRepository(eventExists = true),
            userRepository = FakeUserRepository(userExists = true),
        )

        StepVerifier.create(
            service.createParticipation(
                eventId = 1L,
                command = CreateParticipationCommand(userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
            ),
        )
            .expectNextMatches { participation ->
                participation.id == ParticipationId.of(1L) &&
                    participation.eventId == EventId.of(1L) &&
                    participation.userId == UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")) &&
                    participation.status == ParticipationStatus.ONLINE
            }
            .verifyComplete()

        assertEquals(ParticipationStatus.ONLINE, repository.savedStatus)
    }

    @Test
    fun `createParticipation uses requested status`() {
        val repository = FakeParticipationRepository()
        val service = ParticipationCommandService(
            participationRepository = repository,
            eventRepository = FakeEventRepository(eventExists = true),
            userRepository = FakeUserRepository(userExists = true),
        )

        StepVerifier.create(
            service.createParticipation(
                eventId = 1L,
                command = CreateParticipationCommand(
                    userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    status = ParticipationStatus.ONSITE,
                ),
            ),
        )
            .expectNextMatches { it.status == ParticipationStatus.ONSITE }
            .verifyComplete()
    }

    @Test
    fun `createParticipation throws EventNotFoundException when event missing`() {
        val service = ParticipationCommandService(
            participationRepository = FakeParticipationRepository(),
            eventRepository = FakeEventRepository(eventExists = false),
            userRepository = FakeUserRepository(userExists = true),
        )

        StepVerifier.create(service.createParticipation(999L, CreateParticipationCommand(userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `createParticipation throws UserNotFoundException when user missing`() {
        val service = ParticipationCommandService(
            participationRepository = FakeParticipationRepository(),
            eventRepository = FakeEventRepository(eventExists = true),
            userRepository = FakeUserRepository(userExists = false),
        )

        StepVerifier.create(service.createParticipation(1L, CreateParticipationCommand(userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000999"))))
            .expectError(UserNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `createParticipation throws ParticipationAlreadyExistsException when duplicate participation exists`() {
        val service = ParticipationCommandService(
            participationRepository = FakeParticipationRepository(existingParticipation = existingParticipation()),
            eventRepository = FakeEventRepository(eventExists = true),
            userRepository = FakeUserRepository(userExists = true),
        )

        StepVerifier.create(service.createParticipation(1L, CreateParticipationCommand(userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
            .expectError(ParticipationAlreadyExistsException::class.java)
            .verify()
    }

    @Test
    fun `createParticipation rejects non positive eventId`() {
        val service = ParticipationCommandService(
            participationRepository = FakeParticipationRepository(),
            eventRepository = FakeEventRepository(eventExists = true),
            userRepository = FakeUserRepository(userExists = true),
        )

        assertThrows<InvalidRequestException> {
            service.createParticipation(0L, CreateParticipationCommand(userId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")))
        }
    }

    private fun existingParticipation(): Participation = Participation(
        id = ParticipationId.of(1L),
        userId = UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
        eventId = EventId.of(1L),
        status = ParticipationStatus.ONLINE,
        joinedAt = Instant.parse("2026-05-12T00:00:00Z"),
    )

    private class FakeParticipationRepository(
        private val existingParticipation: Participation? = null,
    ) : ParticipationRepository {
        var savedStatus: ParticipationStatus? = null

        override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> =
            existingParticipation
                ?.takeIf { it.eventId == eventId && it.userId == userId }
                ?.let { Mono.just(it) }
                ?: Mono.empty()

        override fun save(participation: NewParticipation): Mono<Participation> {
            savedStatus = participation.status
            return Mono.just(participation.persist(ParticipationId.of(1L)))
        }
    }

    private class FakeEventRepository(
        private val eventExists: Boolean,
    ) : EventRepository {
        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> =
            if (eventExists && id == EventId.of(1L)) Mono.just(existingEvent()) else Mono.empty()

        override fun save(event: NewEvent): Mono<Event> = Mono.just(event.persist(EventId.of(1L)))

        override fun update(event: Event): Mono<Event> = Mono.just(event)

        override fun deleteById(id: EventId): Mono<Long> = Mono.just(1L)

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(eventExists && id == EventId.of(1L))

        override fun findChatRoomById(id: ChatRoomId): Mono<ChatRoom> = Mono.empty()

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

    private class FakeUserRepository(
        private val userExists: Boolean,
    ) : UserRepository {
        override fun findById(id: UserId): Mono<User> =
            if (userExists && id == UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))) {
                Mono.just(
                    User(
                        id = UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")),
                        nickname = UserNickname.of("ahnnayeong"),
                    ),
                )
            } else {
                Mono.empty()
            }

        override fun existsById(id: UserId): Mono<Boolean> = Mono.just(userExists && id == UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")))

        override fun save(user: NewUser): Mono<User> = Mono.just(user.persist(UserId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))))
    }
}
