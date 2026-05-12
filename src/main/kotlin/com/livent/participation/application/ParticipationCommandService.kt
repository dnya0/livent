package com.livent.participation.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.exception.EventNotFoundException
import com.livent.event.domain.repository.EventRepository
import com.livent.event.domain.value.EventId
import com.livent.participation.domain.exception.ParticipationAlreadyExistsException
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.user.domain.exception.UserNotFoundException
import com.livent.user.domain.repository.UserRepository
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ParticipationCommandService(
    private val participationRepository: ParticipationRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
) {
    fun createParticipation(eventId: Long, command: CreateParticipationCommand): Mono<Participation> {
        val resolvedEventId = resolveEventId(eventId)
        val resolvedUserId = resolveUserId(command.userId)

        return eventRepository.existsById(resolvedEventId)
            .flatMap { exists ->
                if (exists) Mono.just(resolvedEventId) else Mono.error(EventNotFoundException())
            }
            .flatMap {
                userRepository.existsById(resolvedUserId)
            }
            .flatMap { exists ->
                if (exists) Mono.just(resolvedUserId) else Mono.error(UserNotFoundException())
            }
            .flatMap {
                participationRepository.findByEventIdAndUserId(resolvedEventId, resolvedUserId)
                    .flatMap<Participation> { Mono.error(ParticipationAlreadyExistsException()) }
                    .switchIfEmpty(
                        participationRepository.save(
                            NewParticipation.create(
                                userId = resolvedUserId,
                                eventId = resolvedEventId,
                                status = command.status ?: ParticipationStatus.ONLINE,
                            ),
                        ),
                    )
            }
    }

    private fun resolveEventId(eventId: Long): EventId = try {
        EventId.of(eventId)
    } catch (ex: IllegalArgumentException) {
        throw InvalidRequestException("eventId must be a positive number.", ex)
    }

    private fun resolveUserId(userId: Long): UserId = try {
        UserId.of(userId)
    } catch (ex: IllegalArgumentException) {
        throw InvalidRequestException("userId must be a positive number.", ex)
    }
}
