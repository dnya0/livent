package com.livent.message.application

import java.time.Instant
import org.junit.jupiter.api.assertThrows
import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.ChatRoomAccessDeniedException
import com.livent.event.domain.exception.ChatRoomNotFoundException
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

class MessageCommandServiceTest {
    @Test
    fun `sendMessage saves text message when sender joined event`() {
        val messageRepository = FakeMessageRepository()
        val service = MessageCommandService(
            messageRepository = messageRepository,
            eventRepository = FakeEventRepository(globalChatRoom()),
            participationRepository = FakeParticipationRepository(participation(status = ParticipationStatus.ONLINE)),
        )

        StepVerifier.create(
            service.sendMessage(
                chatRoomId = 1L,
                command = SendMessageCommand(senderId = 1L, content = " hello "),
            ),
        )
            .expectNextMatches { message ->
                message.chatRoomId == ChatRoomId.of(1L) &&
                    message.senderId == UserId.of(1L) &&
                    message.content.value == "hello"
            }
            .verifyComplete()
    }

    @Test
    fun `sendMessage rejects missing chat room`() {
        val service = MessageCommandService(
            messageRepository = FakeMessageRepository(),
            eventRepository = FakeEventRepository(chatRoom = null),
            participationRepository = FakeParticipationRepository(participation()),
        )

        StepVerifier.create(service.sendMessage(999L, SendMessageCommand(senderId = 1L, content = "hello")))
            .expectError(ChatRoomNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `sendMessage rejects sender without participation`() {
        val service = MessageCommandService(
            messageRepository = FakeMessageRepository(),
            eventRepository = FakeEventRepository(globalChatRoom()),
            participationRepository = FakeParticipationRepository(participation = null),
        )

        StepVerifier.create(service.sendMessage(1L, SendMessageCommand(senderId = 1L, content = "hello")))
            .expectError(ParticipationNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `sendMessage rejects online participant for local chat room`() {
        val service = MessageCommandService(
            messageRepository = FakeMessageRepository(),
            eventRepository = FakeEventRepository(localChatRoom()),
            participationRepository = FakeParticipationRepository(participation(status = ParticipationStatus.ONLINE)),
        )

        StepVerifier.create(service.sendMessage(2L, SendMessageCommand(senderId = 1L, content = "hello")))
            .expectError(ChatRoomAccessDeniedException::class.java)
            .verify()
    }

    @Test
    fun `sendMessage allows onsite participant for local chat room`() {
        val service = MessageCommandService(
            messageRepository = FakeMessageRepository(),
            eventRepository = FakeEventRepository(localChatRoom()),
            participationRepository = FakeParticipationRepository(participation(status = ParticipationStatus.ONSITE)),
        )

        StepVerifier.create(service.sendMessage(2L, SendMessageCommand(senderId = 1L, content = "hello")))
            .expectNextMatches { it.chatRoomId == ChatRoomId.of(2L) }
            .verifyComplete()
    }

    @Test
    fun `sendMessage rejects blank content before repository access`() {
        val eventRepository = FakeEventRepository(globalChatRoom())
        val participationRepository = FakeParticipationRepository(participation())
        val service = MessageCommandService(
            messageRepository = FakeMessageRepository(),
            eventRepository = eventRepository,
            participationRepository = participationRepository,
        )

        assertThrows<InvalidRequestException> {
            service.sendMessage(1L, SendMessageCommand(senderId = 1L, content = " "))
        }

        assert(!eventRepository.wasCalled)
        assert(!participationRepository.wasCalled)
    }

    private class FakeMessageRepository : MessageRepository {
        override fun findByChatRoomId(chatRoomId: ChatRoomId): Flux<Message> = Flux.empty()

        override fun save(message: NewMessage): Mono<Message> =
            Mono.just(message.persist(MessageId.of(1L)))
    }

    private class FakeEventRepository(
        private val chatRoom: ChatRoom?,
    ) : EventRepository {
        var wasCalled: Boolean = false

        override fun findFirstPage(limit: Int): Flux<Event> = Flux.empty()

        override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> = Flux.empty()

        override fun findById(id: EventId): Mono<Event> = Mono.empty()

        override fun save(event: NewEvent): Mono<Event> = Mono.empty()

        override fun update(event: Event): Mono<Event> = Mono.empty()

        override fun deleteById(id: EventId): Mono<Long> = Mono.empty()

        override fun existsById(id: EventId): Mono<Boolean> = Mono.just(false)

        override fun findChatRoomById(id: ChatRoomId): Mono<ChatRoom> {
            wasCalled = true
            return chatRoom?.takeIf { it.id == id }?.let { Mono.just(it) } ?: Mono.empty()
        }

        override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> = Flux.empty()

        override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> = Mono.empty()
    }

    private class FakeParticipationRepository(
        private val participation: Participation?,
    ) : ParticipationRepository {
        var wasCalled: Boolean = false

        override fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation> {
            wasCalled = true
            return participation
                ?.takeIf { it.eventId == eventId && it.userId == userId }
                ?.let { Mono.just(it) }
                ?: Mono.empty()
        }

        override fun save(participation: NewParticipation): Mono<Participation> = Mono.empty()
    }

    private fun globalChatRoom(): ChatRoom = ChatRoom(
        id = ChatRoomId.of(1L),
        eventId = EventId.of(1L),
        type = ChatRoomType.GLOBAL,
        name = ChatRoomName.of("전체 채팅"),
    )

    private fun localChatRoom(): ChatRoom = ChatRoom(
        id = ChatRoomId.of(2L),
        eventId = EventId.of(1L),
        type = ChatRoomType.LOCAL,
        name = ChatRoomName.of("현장 채팅"),
    )

    private fun participation(status: ParticipationStatus = ParticipationStatus.ONLINE): Participation = Participation(
        id = ParticipationId.of(1L),
        userId = UserId.of(1L),
        eventId = EventId.of(1L),
        status = status,
        joinedAt = Instant.parse("2026-05-12T00:00:00Z"),
    )
}
