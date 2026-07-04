package com.livent.message.application

import java.time.Instant
import com.livent.event.domain.exception.ChatRoomAccessDeniedException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.value.ChatRoomId
import com.livent.event.domain.value.ChatRoomName
import com.livent.event.domain.value.EventId
import com.livent.message.domain.model.Message
import com.livent.message.domain.model.NewMessage
import com.livent.message.domain.repository.MessageRepository
import com.livent.message.domain.type.MessageType
import com.livent.message.domain.value.MessageContent
import com.livent.message.domain.value.MessageId
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.value.UserId
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class MessageQueryServiceTest {
    @Test
    fun `getMessages returns chat room messages for participant`() {
        val service = MessageQueryService(
            messageRepository = FakeMessageRepository(listOf(message(id = 1L), message(id = 2L))),
            eventRepository = FakeEventRepository(chatRoom()),
            participationRepository = FakeParticipationRepository(participation()),
        )

        StepVerifier.create(service.getMessages(chatRoomId = 1L, userId = 1L))
            .expectNextMatches { it.id == MessageId.of(1L) }
            .expectNextMatches { it.id == MessageId.of(2L) }
            .verifyComplete()
    }

    @Test
    fun `getMessages rejects user without participation`() {
        val service = MessageQueryService(
            messageRepository = FakeMessageRepository(listOf(message())),
            eventRepository = FakeEventRepository(chatRoom()),
            participationRepository = FakeParticipationRepository(participation = null),
        )

        StepVerifier.create(service.getMessages(chatRoomId = 1L, userId = 1L))
            .expectError(ParticipationNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `getMessages rejects online participant for local chat room`() {
        val service = MessageQueryService(
            messageRepository = FakeMessageRepository(listOf(message(chatRoomId = 2L))),
            eventRepository = FakeEventRepository(chatRoom(type = ChatRoomType.LOCAL, id = 2L)),
            participationRepository = FakeParticipationRepository(participation(status = ParticipationStatus.ONLINE)),
        )

        StepVerifier.create(service.getMessages(chatRoomId = 2L, userId = 1L))
            .expectError(ChatRoomAccessDeniedException::class.java)
            .verify()
    }

    private class FakeMessageRepository(
        private val messages: List<Message>,
    ) : MessageRepository {
        override fun findByChatRoomId(chatRoomId: ChatRoomId): Flux<Message> =
            Flux.fromIterable(messages.filter { it.chatRoomId == chatRoomId })

        override fun save(message: NewMessage): Mono<Message> = Mono.just(message.persist(MessageId.of(999L)))
    }

    private class FakeEventRepository(
        private val chatRoom: ChatRoom,
    ) : EventRepository {
        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> = Mono.empty()

        override fun save(event: NewEvent): Mono<Event> = Mono.empty()

        override fun update(event: Event): Mono<Event> = Mono.empty()

        override fun deleteById(id: EventId): Mono<Long> = Mono.empty()

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(false)

        override fun findChatRoomById(id: ChatRoomId): Mono<ChatRoom> =
            if (chatRoom.id == id) Mono.just(chatRoom) else Mono.empty()

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> = Flux.empty()

        override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> = Mono.empty()
    }

    private class FakeParticipationRepository(
        private val participation: Participation?,
    ) : ParticipationRepository {
        override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> =
            participation
                ?.takeIf { it.eventId == eventId && it.userId == userId }
                ?.let { Mono.just(it) }
                ?: Mono.empty()

        override fun save(participation: NewParticipation): Mono<Participation> = Mono.empty()
    }

    private fun chatRoom(
        type: ChatRoomType = ChatRoomType.GLOBAL,
        id: Long = 1L,
    ): ChatRoom = ChatRoom(
        id = ChatRoomId.of(id),
        eventId = EventId.of(1L),
        type = type,
        name = ChatRoomName.of("전체 채팅"),
    )

    private fun participation(status: ParticipationStatus = ParticipationStatus.ONLINE): Participation = Participation(
        id = ParticipationId.of(1L),
        userId = UserId.of(1L),
        eventId = EventId.of(1L),
        status = status,
        joinedAt = Instant.parse("2026-05-12T00:00:00Z"),
    )

    private fun message(
        id: Long = 1L,
        chatRoomId: Long = 1L,
    ): Message = Message(
        id = MessageId.of(id),
        chatRoomId = ChatRoomId.of(chatRoomId),
        senderId = UserId.of(1L),
        content = MessageContent.of("hello $id"),
        type = MessageType.TEXT,
        createdAt = Instant.parse("2026-05-12T00:00:00Z").plusSeconds(id),
    )
}
