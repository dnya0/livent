package com.livent.event.adapter.outbound.persistence

import com.livent.event.adapter.outbound.persistence.entity.ChatRoomEntity
import com.livent.event.adapter.outbound.persistence.entity.EventEntity
import com.livent.event.adapter.outbound.persistence.repository.ChatRoomR2dbcRepository
import com.livent.event.adapter.outbound.persistence.repository.EventR2dbcRepository
import com.livent.event.domain.exception.ChatRoomAlreadyExistsException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.ChatRoom
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.value.ChatRoomId
import com.livent.event.domain.value.ChatRoomName
import com.livent.event.domain.value.EventDetails
import com.livent.event.domain.value.EventId
import com.livent.event.domain.value.EventLocation
import com.livent.event.domain.value.EventSchedule
import com.livent.event.domain.value.EventTimezone
import com.livent.event.domain.value.EventTitle
import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class EventPersistenceAdapter(
    private val eventR2dbcRepository: EventR2dbcRepository,
    private val chatRoomR2dbcRepository: ChatRoomR2dbcRepository,
) : EventRepository {

    override fun findFirstPage(limit: Int): Flux<Event> =
        eventR2dbcRepository.findFirstPage(limit = limit)
            .map(EventEntity::toDomain)

    override fun findAfterId(cursor: EventId, limit: Int): Flux<Event> =
        eventR2dbcRepository.findAfterId(cursor = cursor.value, limit = limit)
            .map(EventEntity::toDomain)

    override fun findById(id: EventId): Mono<Event> = eventR2dbcRepository.findById(id.value)
        .map(EventEntity::toDomain)

    override fun save(event: NewEvent): Mono<Event> = eventR2dbcRepository.save(event.toEntity())
        .map(EventEntity::toDomain)

    override fun update(event: Event): Mono<Event> = eventR2dbcRepository.save(event.toEntity())
        .map(EventEntity::toDomain)

    override fun deleteById(id: EventId): Mono<Long> = eventR2dbcRepository.deleteById(id.value)
        .thenReturn(1L)

    override fun existsById(id: EventId): Mono<Boolean> = eventR2dbcRepository.existsById(id.value)

    override fun findChatRoomsByEventId(eventId: EventId): Flux<ChatRoom> =
        chatRoomR2dbcRepository.findByEventIdOrderByTypeAsc(eventId.value).map(ChatRoomEntity::toDomain)

    override fun saveChatRoom(chatRoom: NewChatRoom): Mono<ChatRoom> =
        chatRoomR2dbcRepository.save(chatRoom.toEntity())
            .onErrorMap(::isChatRoomTypeUniqueViolation) { ChatRoomAlreadyExistsException() }
            .onErrorMap(::isChatRoomEventForeignKeyViolation) { EventNotFoundException() }
            .map(ChatRoomEntity::toDomain)
}

private fun isChatRoomTypeUniqueViolation(ex: Throwable): Boolean {
    val errors = generateSequence(ex) { it.cause }.toList()
    val isIntegrityViolation = errors.any {
        it is DuplicateKeyException ||
            it is DataIntegrityViolationException ||
            it is R2dbcDataIntegrityViolationException
    }
    val hasChatRoomUniqueConstraint = errors
        .mapNotNull(Throwable::message)
        .any { message ->
            message.contains("uk_chat_rooms_event_type", ignoreCase = true) ||
                message.contains("duplicate key", ignoreCase = true) ||
                message.contains("23505")
        }

    return isIntegrityViolation && hasChatRoomUniqueConstraint
}

private fun isChatRoomEventForeignKeyViolation(ex: Throwable): Boolean {
    val errors = generateSequence(ex) { it.cause }.toList()
    val isIntegrityViolation = errors.any {
        it is DataIntegrityViolationException ||
            it is R2dbcDataIntegrityViolationException
    }
    val hasEventForeignKeySignal = errors
        .mapNotNull(Throwable::message)
        .any { message ->
            message.contains("chat_rooms_event_id_fkey", ignoreCase = true) ||
                message.contains("23503") ||
                message.contains("23506") ||
                message.contains("referential integrity constraint violation", ignoreCase = true)
        }

    return isIntegrityViolation && hasEventForeignKeySignal
}

private fun EventEntity.toDomain(): Event = Event(
    id = EventId.of(requireNotNull(id) { "Event id must not be null when reading (title='$title')." }),
    details = EventDetails(
        title = EventTitle.of(title),
        location = EventLocation.of(location),
        schedule = EventSchedule(startTime = startTime, endTime = endTime, timezone = EventTimezone.of(timezone)),
        visibility = visibility,
    ),
)

private fun NewEvent.toEntity(id: Long? = null): EventEntity = EventEntity(
    id = id,
    title = title.value,
    location = location.value,
    startTime = schedule.startTime,
    endTime = schedule.endTime,
    timezone = schedule.timezone.id,
    visibility = visibility,
)

private fun Event.toEntity(): EventEntity = EventEntity(
    id = id.value,
    title = title.value,
    location = location.value,
    startTime = schedule.startTime,
    endTime = schedule.endTime,
    timezone = schedule.timezone.id,
    visibility = visibility,
)

private fun NewChatRoom.toEntity(id: Long? = null): ChatRoomEntity = ChatRoomEntity(
    id = id,
    eventId = eventId.value,
    type = type,
    name = name.value,
)

private fun ChatRoomEntity.toDomain(): ChatRoom = ChatRoom(
    id = ChatRoomId.of(requireNotNull(id) { "ChatRoom id must not be null when reading (eventId=$eventId, type=$type)." }),
    eventId = EventId.of(eventId),
    type = type,
    name = ChatRoomName.of(name),
)
