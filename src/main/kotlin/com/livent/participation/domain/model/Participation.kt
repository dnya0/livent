package com.livent.participation.domain.model

import java.time.Instant
import com.livent.event.domain.value.EventId
import com.livent.participation.domain.type.ParticipationStatus
import com.livent.participation.domain.value.ParticipationId
import com.livent.user.domain.value.UserId

data class Participation(
    val id: ParticipationId,
    val userId: UserId,
    val eventId: EventId,
    val status: ParticipationStatus,
    val joinedAt: Instant,
)

data class NewParticipation(
    val userId: UserId,
    val eventId: EventId,
    val status: ParticipationStatus,
    val joinedAt: Instant,
) {
    fun persist(id: ParticipationId): Participation = Participation(
        id = id,
        userId = userId,
        eventId = eventId,
        status = status,
        joinedAt = joinedAt,
    )

    companion object {
        fun create(
            userId: UserId,
            eventId: EventId,
            status: ParticipationStatus = ParticipationStatus.ONLINE,
            joinedAt: Instant = Instant.now(),
        ): NewParticipation = NewParticipation(
            userId = userId,
            eventId = eventId,
            status = status,
            joinedAt = joinedAt,
        )
    }
}
