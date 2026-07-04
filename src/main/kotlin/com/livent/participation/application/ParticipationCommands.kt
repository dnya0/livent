package com.livent.participation.application

import java.util.UUID
import com.livent.participation.domain.type.ParticipationStatus

data class CreateParticipationCommand(
    val userId: UUID,
    val status: ParticipationStatus? = null,
)
