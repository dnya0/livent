package com.livent.participation.application

import com.livent.participation.domain.type.ParticipationStatus

data class CreateParticipationCommand(
    val userId: Long,
    val status: ParticipationStatus? = null,
)
