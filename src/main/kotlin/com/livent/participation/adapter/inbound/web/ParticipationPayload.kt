package com.livent.participation.adapter.inbound.web

import java.util.UUID
import com.livent.participation.domain.type.ParticipationStatus
import jakarta.validation.constraints.NotNull

data class ParticipationPayload(
    @field:NotNull(message = "userId must not be null.")
    val userId: UUID,
    val status: ParticipationStatus? = null,
)
