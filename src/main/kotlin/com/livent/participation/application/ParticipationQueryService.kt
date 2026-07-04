package com.livent.participation.application

import java.util.UUID
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
    fun getParticipation(eventId: Long, userId: UUID): Mono<Participation> =
        participationRepository.findByEventIdAndUserId(
            eventId = resolveEventId(eventId),
            userId = UserId.of(userId),
        )
            .switchIfEmpty(Mono.error(ParticipationNotFoundException()))

    private fun resolveEventId(eventId: Long): EventId = invalidRequestCatch(
        message = "eventId must be a positive number.",
    ) {
        EventId.of(eventId)
    }
}
