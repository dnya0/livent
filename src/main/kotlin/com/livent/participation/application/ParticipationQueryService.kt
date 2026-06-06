package com.livent.participation.application

import com.livent.common.adapter.inbound.web.exception.InvalidRequestException
import com.livent.event.domain.value.EventId
import com.livent.participation.domain.exception.ParticipationNotFoundException
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.repository.ParticipationRepository
import com.livent.user.domain.value.UserId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ParticipationQueryService(
    private val participationRepository: ParticipationRepository,
) {
    fun getParticipation(eventId: Long, userId: Long): Mono<Participation> =
        participationRepository.findByEventIdAndUserId(
            eventId = resolveEventId(eventId),
            userId = resolveUserId(userId),
        )
            .switchIfEmpty(Mono.error(ParticipationNotFoundException()))

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
