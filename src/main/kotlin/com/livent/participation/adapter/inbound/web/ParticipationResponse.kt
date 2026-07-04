package com.livent.participation.adapter.inbound.web

import java.time.Instant
import java.util.UUID
import com.livent.participation.domain.model.Participation
import com.livent.participation.domain.type.ParticipationStatus

data class ParticipationResponse(
    val id: Long,
    val userId: UUID,
    val eventId: Long,
    val status: ParticipationStatus,
    val joinedAt: Instant,
)

fun Participation.toResponse(): ParticipationResponse = ParticipationResponse(
    id = id.value,
    userId = userId.value,
    eventId = eventId.value,
    status = status,
    joinedAt = joinedAt,
)
