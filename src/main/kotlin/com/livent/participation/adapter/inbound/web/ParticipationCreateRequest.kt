package com.livent.participation.adapter.inbound.web

import com.livent.participation.application.CreateParticipationCommand

typealias ParticipationCreateRequest = ParticipationPayload

fun ParticipationCreateRequest.toCreateCommand(): CreateParticipationCommand = CreateParticipationCommand(
    userId = userId,
    status = status,
)
