package com.livent.event.application

import com.livent.common.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.repository.ChatRoomRepository
import com.livent.event.domain.model.type.ChatRoomType
import com.livent.event.domain.model.Event
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.model.type.EventVisibility
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.OffsetDateTime

class EventQueryServiceTest {
    private val baseTime: OffsetDateTime = OffsetDateTime.parse("2026-04-07T10:00:00+09:00")

    @Test
    fun `getEvent returns event when found`() {
        val service = EventQueryService(FakeEventRepository(), FakeChatRoomRepository())

        StepVerifier.create(service.getEvent(1L))
            .expectNextMatches { it.id == 1L && it.title == "Seoul Tech Meetup" }
            .verifyComplete()
    }

    @Test
    fun `getEvent throws EventNotFoundException when event missing`() {
        val service = EventQueryService(FakeEventRepository(), FakeChatRoomRepository())

        StepVerifier.create(service.getEvent(999L))
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getChatRooms requires existing event before loading rooms`() {
        val service = EventQueryService(FakeEventRepository(), FakeChatRoomRepository())

        StepVerifier.create(service.getChatRooms(1L))
            .expectNextCount(2)
            .verifyComplete()
    }

    private inner class FakeEventRepository : EventRepository {
        private val events = listOf(
            Event(
                id = 1L,
                title = "Seoul Tech Meetup",
                location = "COEX",
                startTime = baseTime,
                endTime = baseTime.plusHours(2),
                visibility = EventVisibility.BOTH,
            ),
        )

        override fun findAll(): Flux<Event> = Flux.fromIterable(events)

        override fun findById(id: Long): Mono<Event> =
            events.firstOrNull { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()
    }

    private inner class FakeChatRoomRepository : ChatRoomRepository {
        override fun findByEventId(eventId: Long): Flux<ChatRoom> =
            Flux.just(
                ChatRoom(id = 1L, eventId = eventId, type = ChatRoomType.GLOBAL, name = "전체 채팅"),
                ChatRoom(id = 2L, eventId = eventId, type = ChatRoomType.LOCAL, name = "현장 채팅"),
            )
    }
}
