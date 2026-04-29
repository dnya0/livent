package com.livent.event.application

import java.time.Instant
import kotlin.test.assertEquals
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.ChatRoomAlreadyExistsException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.type.EventVisibility
import com.livent.event.domain.value.ChatRoomId
import com.livent.event.domain.value.ChatRoomName
import com.livent.event.domain.value.EventDetails
import com.livent.event.domain.value.EventId
import com.livent.event.domain.value.EventLocation
import com.livent.event.domain.value.EventSchedule
import com.livent.event.domain.value.EventTimezone
import com.livent.event.domain.value.EventTitle
import com.livent.event.domain.repository.EventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EventCommandServiceTest {
    @Test
    fun `createEvent saves event with trimmed fields and timezone`() {
        val repository = FakeEventRepository()
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.createEvent(
                CreateEventCommand(
                    title = "  Seoul Tech Meetup  ",
                    location = "  COEX  ",
                    startTime = Instant.parse("2026-04-20T10:00:00Z"),
                    endTime = Instant.parse("2026-04-20T12:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.BOTH,
                ),
            ),
        )
            .expectNextMatches { event ->
                event.id == EventId.of(1L) &&
                    event.title.value == "Seoul Tech Meetup" &&
                    event.location.value == "COEX" &&
                    event.schedule.timezone == EventTimezone.of("Asia/Seoul") &&
                    event.visibility == EventVisibility.BOTH
            }
            .verifyComplete()

        assertEquals(listOf(ChatRoomType.GLOBAL), repository.chatRoomsFor(1L).map(ChatRoom::type))
    }

    @Test
    fun `createEvent rejects invalid timezone`() {
        val service = EventCommandService(FakeEventRepository())

        assertThrows<InvalidRequestException> {
            service.createEvent(
                CreateEventCommand(
                    title = "Seoul Tech Meetup",
                    location = "COEX",
                    startTime = Instant.parse("2026-04-20T10:00:00Z"),
                    endTime = Instant.parse("2026-04-20T12:00:00Z"),
                    timezone = "Invalid/Timezone",
                    visibility = EventVisibility.BOTH,
                ),
            )
        }
    }

    @Test
    fun `createEvent rejects reversed time range`() {
        val service = EventCommandService(FakeEventRepository())

        assertThrows<IllegalArgumentException> {
            service.createEvent(
                CreateEventCommand(
                    title = "Seoul Tech Meetup",
                    location = "COEX",
                    startTime = Instant.parse("2026-04-20T12:00:00Z"),
                    endTime = Instant.parse("2026-04-20T10:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.BOTH,
                ),
            )
        }
    }

    @Test
    fun `createEvent rejects blank title after normalization`() {
        val service = EventCommandService(FakeEventRepository())

        assertThrows<IllegalArgumentException> {
            service.createEvent(
                CreateEventCommand(
                    title = "   ",
                    location = "COEX",
                    startTime = Instant.parse("2026-04-20T10:00:00Z"),
                    endTime = Instant.parse("2026-04-20T12:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.BOTH,
                ),
            )
        }
    }

    @Test
    fun `createEvent rejects too long title`() {
        val service = EventCommandService(FakeEventRepository())

        assertThrows<IllegalArgumentException> {
            service.createEvent(
                CreateEventCommand(
                    title = "a".repeat(256),
                    location = "COEX",
                    startTime = Instant.parse("2026-04-20T10:00:00Z"),
                    endTime = Instant.parse("2026-04-20T12:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.BOTH,
                ),
            )
        }
    }

    @Test
    fun `event aggregate can update details through behaviors`() {
        val event = NewEvent.create(
            title = "Seoul Tech Meetup",
            location = "COEX",
            startTime = Instant.parse("2026-04-20T10:00:00Z"),
            endTime = Instant.parse("2026-04-20T12:00:00Z"),
            timezone = EventTimezone.of("Asia/Seoul"),
            visibility = EventVisibility.BOTH,
        ).persist(EventId.of(1L))
            .rename("Platform Meetup")
            .relocate("DDP")
            .changeVisibility(EventVisibility.ONSITE)

        assertEquals("Platform Meetup", event.title.value)
        assertEquals("DDP", event.location.value)
        assertEquals(EventVisibility.ONSITE, event.visibility)
    }

    @Test
    fun `updateEvent updates persisted aggregate through behaviors`() {
        val repository = FakeEventRepository()
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.updateEvent(
                eventId = 1L,
                command = UpdateEventCommand(
                    title = "Platform Meetup",
                    location = "DDP",
                    startTime = Instant.parse("2026-04-21T10:00:00Z"),
                    endTime = Instant.parse("2026-04-21T13:00:00Z"),
                    timezone = "Asia/Tokyo",
                    visibility = EventVisibility.ONSITE,
                ),
            ),
        )
            .expectNextMatches { event ->
                event.id == EventId.of(1L) &&
                    event.title.value == "Platform Meetup" &&
                    event.location.value == "DDP" &&
                    event.schedule.timezone == EventTimezone.of("Asia/Tokyo") &&
                    event.visibility == EventVisibility.ONSITE
            }
            .verifyComplete()
    }

    @Test
    fun `updateEvent throws EventNotFoundException when event missing`() {
        val service = EventCommandService(FakeEventRepository())

        StepVerifier.create(
            service.updateEvent(
                eventId = 999L,
                command = UpdateEventCommand(
                    title = "Platform Meetup",
                    location = "DDP",
                    startTime = Instant.parse("2026-04-21T10:00:00Z"),
                    endTime = Instant.parse("2026-04-21T13:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.ONSITE,
                ),
            ),
        )
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `updateEvent rejects non-positive eventId`() {
        val service = EventCommandService(FakeEventRepository())

        assertThrows<InvalidRequestException> {
            service.updateEvent(
                eventId = 0L,
                command = UpdateEventCommand(
                    title = "Platform Meetup",
                    location = "DDP",
                    startTime = Instant.parse("2026-04-21T10:00:00Z"),
                    endTime = Instant.parse("2026-04-21T13:00:00Z"),
                    timezone = "Asia/Seoul",
                    visibility = EventVisibility.ONSITE,
                ),
            )
        }
    }

    @Test
    fun `deleteEvent completes when event exists`() {
        val service = EventCommandService(FakeEventRepository())

        StepVerifier.create(service.deleteEvent(1L))
            .expectNextMatches { it.deletedCount == 1L }
            .verifyComplete()
    }

    @Test
    fun `deleteEvent throws EventNotFoundException when event missing`() {
        val service = EventCommandService(FakeEventRepository())

        StepVerifier.create(service.deleteEvent(999L))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `createChatRoom creates room when type is allowed and unique`() {
        val repository = FakeEventRepository()
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.createChatRoom(
                eventId = 1L,
                command = CreateChatRoomCommand(
                    type = ChatRoomType.LOCAL,
                    name = "  현장 참가자  ",
                ),
            ),
        )
            .expectNextMatches { chatRoom ->
                chatRoom.eventId == EventId.of(1L) &&
                    chatRoom.type == ChatRoomType.LOCAL &&
                    chatRoom.name == ChatRoomName.of("현장 참가자")
            }
            .verifyComplete()
    }

    @Test
    fun `createChatRoom rejects duplicate type`() {
        val repository = FakeEventRepository().apply {
            saveChatRoom(
                NewChatRoom.create(
                    eventId = EventId.of(1L),
                    type = ChatRoomType.GLOBAL,
                ),
            ).block()
        }
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.createChatRoom(
                eventId = 1L,
                command = CreateChatRoomCommand(type = ChatRoomType.GLOBAL),
            ),
        )
            .expectError(InvalidRequestException::class.java)
            .verify()
    }

    @Test
    fun `createChatRoom propagates repository conflict error`() {
        val repository = FakeEventRepository(
            saveChatRoomError = ChatRoomAlreadyExistsException(),
        )
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.createChatRoom(
                eventId = 1L,
                command = CreateChatRoomCommand(type = ChatRoomType.GLOBAL),
            ),
        )
            .expectError(ChatRoomAlreadyExistsException::class.java)
            .verify()
    }

    @Test
    fun `createChatRoom rejects local room for online event`() {
        val repository = FakeEventRepository(
            existingEvent = Event(
                id = EventId.of(1L),
                details = EventDetails(
                    title = EventTitle.of("Online Summit"),
                    location = EventLocation.of("Zoom"),
                    schedule = EventSchedule(
                        startTime = Instant.parse("2026-04-20T10:00:00Z"),
                        endTime = Instant.parse("2026-04-20T12:00:00Z"),
                        timezone = EventTimezone.of("Asia/Seoul"),
                    ),
                    visibility = EventVisibility.ONLINE,
                ),
            ),
        )
        val service = EventCommandService(repository)

        StepVerifier.create(
            service.createChatRoom(
                eventId = 1L,
                command = CreateChatRoomCommand(type = ChatRoomType.LOCAL),
            ),
        )
            .expectError(InvalidRequestException::class.java)
            .verify()
    }

    private class FakeEventRepository(
        private val existingEvent: Event = Event(
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
        ),
        private val saveChatRoomError: Throwable? = null,
    ) : EventRepository {
        private val chatRooms = mutableListOf<ChatRoom>()
        private var nextChatRoomId = 1L

        fun chatRoomsFor(eventId: Long): List<ChatRoom> = chatRooms.filter { it.eventId == EventId.of(eventId) }

        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> =
            if (id == existingEvent.id) Mono.just(existingEvent) else Mono.empty()

        override fun save(event: NewEvent): Mono<Event> = Mono.just(
            event.persist(EventId.of(1L)),
        )

        override fun update(event: Event): Mono<Event> = Mono.just(event)

        override fun deleteById(id: EventId): Mono<Long> = Mono.just(1L)

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(existingEvent.id == id)

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> =
            Flux.fromIterable(chatRooms.filter { it.eventId == eventId })

        override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> {
            saveChatRoomError?.let { return Mono.error(it) }
            val persisted = chatRoom.persist(ChatRoomId.of(nextChatRoomId++))
            chatRooms += persisted
            return Mono.just(persisted)
        }
    }
}
