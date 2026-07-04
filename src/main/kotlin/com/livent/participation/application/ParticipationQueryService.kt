package com.livent.participation.application

import com.livent.common.adapter.inbound.web.exception.invalidRequestCatch
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

    private fun resolveEventId(eventId: Long): EventId = invalidRequestCatch(
        message = "eventId must be a positive number.",
    ) {
        EventId.of(eventId)
    }

    private fun resolveUserId(userId: Long): UserId = invalidRequestCatch(
        message = "userId must be a positive number.",
    ) {
        UserId.of(userId)
    }
}
