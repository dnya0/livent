package com.livent.event.application

import com.livent.common.adapter.inbound.web.exception.invalidRequestCatch
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.EventChatRoomPolicy
import com.livent.event.domain.model.NewChatRoom
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.type.ChatRoomType
import com.livent.event.domain.value.EventId
import com.livent.event.domain.value.EventTimezone
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class EventCommandService(
    private val eventRepository: EventRepository,
) {
    @Transactional
    fun createEvent(command: CreateEventCommand): Mono<Event> = eventRepository.save(
        NewEvent.create(
            title = command.title,
            location = command.location,
            startTime = command.startTime,
            endTime = command.endTime,
            timezone = parseTimezone(command.timezone),
            visibility = command.visibility,
        ),
    )
        .flatMap { event ->
            Flux.fromIterable(EventChatRoomPolicy.defaultChatRoomsFor(event))
                .concatMap(eventRepository::saveChatRoom)
                .then(Mono.just(event))
        }

    fun updateEvent(eventId: Long, command: UpdateEventCommand): Mono<Event> {
        val id = resolveEventId(eventId)
        val timezone = parseTimezone(command.timezone)

        return eventRepository.findById(id)
            .switchIfEmpty(Mono.error(EventNotFoundException()))
            .map { event ->
                event.rename(command.title)
                    .relocate(command.location)
                    .reschedule(
                        startTime = command.startTime,
                        endTime = command.endTime,
                        timezone = timezone,
                    )
                    .changeVisibility(command.visibility)
            }
            .flatMap(eventRepository::update)
    }

    fun deleteEvent(eventId: Long): Mono<DeleteEventResult> {
        val id = resolveEventId(eventId)

        return eventRepository.existsById(id)
            .flatMap { exists ->
                if (exists) eventRepository.deleteById(id).map(::DeleteEventResult)
                else Mono.error(EventNotFoundException())
            }
    }

    @Transactional
    fun createChatRoom(eventId: Long, command: CreateChatRoomCommand) =
        eventRepository.findById(resolveEventId(eventId))
            .switchIfEmpty(Mono.error(EventNotFoundException()))
            .flatMap { event ->
                eventRepository.findChatRoomsByEventId(event.id)
                    .map { it.type }
                    .collectList()
                    .map { it.toSet() }
                    .map { existingTypes ->
                        validateChatRoomPolicy(
                            event = event,
                            type = command.type,
                            existingTypes = existingTypes,
                        )
                        event
                    }
            }
            .flatMap { event ->
                eventRepository.saveChatRoom(
                    NewChatRoom.create(
                        eventId = event.id,
                        type = command.type,
                        name = command.name,
                    ),
                )
            }

    private fun parseTimezone(timezone: String): EventTimezone = invalidRequestCatch(
        message = "timezone must be a valid IANA timezone.",
    ) {
        EventTimezone.of(timezone)
    }

    private fun resolveEventId(eventId: Long): EventId = invalidRequestCatch(
        message = "eventId must be a positive number.",
    ) {
        EventId.of(eventId)
    }

    private fun validateChatRoomPolicy(
        event: Event,
        type: ChatRoomType,
        existingTypes: Set<ChatRoomType>,
    ) {
        invalidRequestCatch(
            message = "invalid chat room policy.",
        ) {
            EventChatRoomPolicy.validateCreatable(
                event = event,
                type = type,
                existingTypes = existingTypes,
            )
        }
    }
}

data class DeleteEventResult(
    val deletedCount: Long,
)
