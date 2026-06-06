package com.livent.participation.domain.repository

import com.livent.event.domain.value.EventId
import com.livent.participation.domain.model.NewParticipation
import com.livent.participation.domain.model.Participation
import com.livent.user.domain.value.UserId
import reactor.core.publisher.Mono

interface ParticipationRepository {
    fun findByEventIdAndUserId(eventId: EventId, userId: UserId): Mono<Participation>

    fun save(participation: NewParticipation): Mono<Participation>
}
