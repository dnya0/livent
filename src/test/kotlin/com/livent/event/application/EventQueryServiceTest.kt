package com.livent.event.application

import java.time.Instant
import java.time.ZoneId
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.type.EventVisibility
import com.livent.event.domain.model.value.EventSchedule
import com.livent.event.domain.repository.EventRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EventQueryServiceTest {
    private val seoulZone: ZoneId = ZoneId.of("Asia/Seoul")
    private val baseTime: Instant = Instant.parse("2026-04-07T01:00:00Z")

    @Test
    fun `getEvent returns event when found`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEvent(1L))
            .expectNextMatches { it.id == 1L && it.title == "Seoul Tech Meetup" }
            .verifyComplete()
    }

    @Test
    fun `getEvent throws EventNotFoundException when event missing`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEvent(999L))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getChatRooms requires existing event before loading rooms`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getChatRooms(1L))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getChatRooms throws EventNotFoundException when event missing`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getChatRooms(999L))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getEventSlice returns first page when cursor is absent`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEventSlice(cursor = null, size = 2))
            .expectNextMatches { slice ->
                slice.items.map { it.id } == listOf(1L, 2L) &&
                    slice.size == 2 &&
                    slice.hasNext &&
                    slice.nextCursor == "2"
            }
            .verifyComplete()
    }

    @Test
    fun `getEventSlice returns events after cursor`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEventSlice(cursor = "1", size = 2))
            .expectNextMatches { slice ->
                slice.items.map { it.id } == listOf(2L, 3L) &&
                    slice.size == 2 &&
                    !slice.hasNext &&
                    slice.nextCursor == null
            }
            .verifyComplete()
    }

    @Test
    fun `getEventSlice caps page size to max limit`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEventSlice(cursor = null, size = 1000))
            .expectNextMatches { slice ->
                slice.items.size == 3 && slice.size == 3 && !slice.hasNext && slice.nextCursor == null
            }
            .verifyComplete()
    }

    @Test
    fun `getEventSlice returns nextCursor only when more events exist`() {
        val service = EventQueryService(FakeEventRepository())

        StepVerifier.create(service.getEventSlice(cursor = null, size = 1))
            .expectNextMatches { slice ->
                slice.items.map { it.id } == listOf(1L) &&
                    slice.size == 1 &&
                    slice.hasNext &&
                    slice.nextCursor == "1"
            }
            .verifyComplete()
    }

    @Test
    fun `getEventSlice rejects negative cursor`() {
        val service = EventQueryService(FakeEventRepository())

        assertThrows<InvalidRequestException> {
            service.getEventSlice(cursor = "-1", size = 20)
        }
    }

    @Test
    fun `getEventSlice rejects invalid cursor format`() {
        val service = EventQueryService(FakeEventRepository())

        assertThrows<InvalidRequestException> {
            service.getEventSlice(cursor = "invalid", size = 20)
        }
    }

    private inner class FakeEventRepository : EventRepository {
        private val events = listOf(
            Event(
                id = 1L,
                title = "Seoul Tech Meetup",
                location = "COEX",
                schedule = EventSchedule(startTime = baseTime, endTime = baseTime.plusSeconds(7200), timezone = seoulZone),
                visibility = EventVisibility.BOTH,
            ),
            Event(
                id = 2L,
                title = "Busan Dev Conference",
                location = "BEXCO",
                schedule = EventSchedule(startTime = baseTime.plusSeconds(86400), endTime = baseTime.plusSeconds(86400 + 14400), timezone = seoulZone),
                visibility = EventVisibility.ONSITE,
            ),
            Event(
                id = 3L,
                title = "Incheon Startup Night",
                location = "Songdo",
                schedule = EventSchedule(startTime = baseTime.plusSeconds(172800), endTime = baseTime.plusSeconds(172800 + 10800), timezone = seoulZone),
                visibility = EventVisibility.ONLINE,
            ),
        )

        override fun findFirstPage(limit: Int): Flux<Event> =
            Flux.fromIterable(events.take(limit))

        override fun findAfterId(cursor: Long, limit: Int): Flux<Event> =
            Flux.fromIterable(events.filter { it.id > cursor }.take(limit))

        override fun findById(id: Long): Mono<Event> =
            events.firstOrNull { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()

        override fun existsById(id: Long): Mono<Boolean> =
            Mono.just(events.any { it.id == id })

        override fun findChatRoomsByEventId(eventId: Long): Flux<ChatRoom> =
            if (events.none { it.id == eventId }) Flux.empty()
            else Flux.just(
                ChatRoom(id = 1L, eventId = eventId, type = ChatRoomType.GLOBAL, name = "전체 채팅"),
                ChatRoom(id = 2L, eventId = eventId, type = ChatRoomType.LOCAL, name = "현장 채팅"),
            )
    }
}
