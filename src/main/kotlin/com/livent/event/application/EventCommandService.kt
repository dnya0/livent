package com.livent.event.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.model.Event
import com.livent.event.domain.model.NewEvent
import com.livent.event.domain.model.value.EventId
import com.livent.event.domain.model.value.EventTimezone
import com.livent.event.domain.repository.EventRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class EventCommandService(
    private val eventRepository: EventRepository,
) {
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

    private fun parseTimezone(timezone: String): EventTimezone = try {
        EventTimezone.of(timezone)
    } catch (ex: IllegalArgumentException) {
        throw InvalidRequestException("timezone must be a valid IANA timezone.", ex)
    }

    private fun resolveEventId(eventId: Long): EventId = try {
        EventId.of(eventId)
    } catch (ex: IllegalArgumentException) {
        throw InvalidRequestException("eventId must be a positive number.", ex)
    }
}

data class DeleteEventResult(
    val deletedCount: Long,
)
