package com.livent.event.adapter.outbound.persistence

import java.lang.reflect.Proxy
import com.livent.event.adapter.outbound.persistence.entity.ChatRoomEntity
import com.livent.event.adapter.outbound.persistence.entity.EventEntity
import com.livent.event.adapter.outbound.persistence.repository.ChatRoomR2dbcRepository
import com.livent.event.adapter.outbound.persistence.repository.EventR2dbcRepository
import com.livent.event.domain.exception.ChatRoomAlreadyExistsException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.value.EventId
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class EventPersistenceAdapterTest {
    @Test
    fun `saveChatRoom maps unique constraint violation to conflict domain error`() {
        val adapter = EventPersistenceAdapter(
            eventR2dbcRepository = stubEventRepository(),
            chatRoomR2dbcRepository = stubChatRoomRepository(
                saveResult = Mono.error(
                    R2dbcDataIntegrityViolationException(
                        "duplicate key value violates unique constraint uk_chat_rooms_event_type",
                        "23505",
                        0,
                    ),
                ),
            ),
        )

        StepVerifier.create(
            adapter.saveChatRoom(
                NewChatRoom.create(
                    eventId = EventId.of(1L),
                    type = ChatRoomType.GLOBAL,
                ),
            ),
        )
            .expectError(ChatRoomAlreadyExistsException::class.java)
            .verify()
    }

    @Test
    fun `saveChatRoom maps foreign key violation to event not found`() {
        val adapter = EventPersistenceAdapter(
            eventR2dbcRepository = stubEventRepository(),
            chatRoomR2dbcRepository = stubChatRoomRepository(
                saveResult = Mono.error(
                    R2dbcDataIntegrityViolationException(
                        "referential integrity constraint violation: chat_rooms_event_id_fkey",
                        "23503",
                        0,
                    ),
                ),
            ),
        )

        StepVerifier.create(
            adapter.saveChatRoom(
                NewChatRoom.create(
                    eventId = EventId.of(1L),
                    type = ChatRoomType.GLOBAL,
                ),
            ),
        )
            .expectError(EventNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `saveChatRoom preserves non constraint failures`() {
        val failure = IllegalStateException("boom")
        val adapter = EventPersistenceAdapter(
            eventR2dbcRepository = stubEventRepository(),
            chatRoomR2dbcRepository = stubChatRoomRepository(saveResult = Mono.error(failure)),
        )

        StepVerifier.create(
            adapter.saveChatRoom(
                NewChatRoom.create(
                    eventId = EventId.of(1L),
                    type = ChatRoomType.GLOBAL,
                ),
            ),
        )
            .expectErrorMatches { it === failure }
            .verify()
    }

    @Suppress("UNCHECKED_CAST")
    private fun stubEventRepository(): EventR2dbcRepository = Proxy.newProxyInstance(
        EventR2dbcRepository::class.java.classLoader,
        arrayOf(EventR2dbcRepository::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "findById" -> Mono.empty<EventEntity>()
            "findFirstPage" -> Flux.empty<EventEntity>()
            "findAfterId" -> Flux.empty<EventEntity>()
            "existsById" -> Mono.just(false)
            "deleteById" -> Mono.empty<Void>()
            "save" -> unsupported(method.name)
            else -> unsupported(method.name)
        }
    } as EventR2dbcRepository

    @Suppress("UNCHECKED_CAST")
    private fun stubChatRoomRepository(saveResult: Mono<ChatRoomEntity>): ChatRoomR2dbcRepository =
        Proxy.newProxyInstance(
            ChatRoomR2dbcRepository::class.java.classLoader,
            arrayOf(ChatRoomR2dbcRepository::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "save" -> saveResult
                "findByEventIdOrderByTypeAsc" -> Flux.empty<ChatRoomEntity>()
                "findById" -> Mono.empty<ChatRoomEntity>()
                "existsById" -> Mono.just(false)
                "deleteById" -> Mono.empty<Void>()
                else -> unsupported(method.name)
            }
        } as ChatRoomR2dbcRepository

    private fun unsupported(name: String): Nothing = throw UnsupportedOperationException("Unexpected call: $name")
}
