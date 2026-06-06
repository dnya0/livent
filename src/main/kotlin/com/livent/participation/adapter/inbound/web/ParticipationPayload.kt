package com.livent.participation.adapter.inbound.web

import com.livent.participation.domain.type.ParticipationStatus
import jakarta.validation.constraints.NotNull

data class ParticipationPayload(
    @field:NotNull(message = "userId must not be null.")
    val userId: Long,
    val status: ParticipationStatus? = null,
)
