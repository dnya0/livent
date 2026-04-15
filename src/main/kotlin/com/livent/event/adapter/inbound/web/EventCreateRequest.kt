package com.livent.event.adapter.inbound.web

import com.livent.event.application.CreateEventCommand

typealias EventCreateRequest = EventPayload

fun EventCreateRequest.toCreateCommand(): CreateEventCommand {
    val payload = toCommandPayload()

    return CreateEventCommand(
        title = payload.title,
        location = payload.location,
        startTime = payload.startTime,
        endTime = payload.endTime,
        timezone = payload.timezone,
        visibility = payload.visibility,
    )
}
