package com.livent.event.application

import java.time.Instant
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.model.value.EventDetails
import com.livent.event.domain.model.type.EventVisibility
import com.livent.event.domain.model.value.EventId
import com.livent.event.domain.model.value.EventLocation
import com.livent.event.domain.model.value.EventSchedule
import com.livent.event.domain.model.value.EventTitle
import com.livent.event.domain.model.value.EventTimezone
import com.livent.event.domain.repository.EventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EventCommandServiceTest {
    private val existingEvent = Event(
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
            .verifyComplete()
    }

    @Test
    fun `deleteEvent throws EventNotFoundException when event missing`() {
        val service = EventCommandService(FakeEventRepository())

        StepVerifier.create(service.deleteEvent(999L))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    private class FakeEventRepository : EventRepository {
        private val existingEvent = Event(
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

        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> =
            if (id == existingEvent.id) Mono.just(existingEvent) else Mono.empty()

        override fun save(event: NewEvent): Mono<Event> = Mono.just(
            event.persist(EventId.of(1L)),
        )

        override fun update(event: Event): Mono<Event> = Mono.just(event)

        override fun deleteById(id: EventId): Mono<Void> = Mono.empty()

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(false)

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> = Flux.empty()
    }
}
