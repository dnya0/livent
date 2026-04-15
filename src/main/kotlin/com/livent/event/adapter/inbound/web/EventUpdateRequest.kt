package com.livent.event.adapter.inbound.web

import com.livent.event.application.UpdateEventCommand

typealias EventUpdateRequest = EventPayload

fun EventUpdateRequest.toUpdateCommand(): UpdateEventCommand {
    val payload = toCommandPayload()

    return UpdateEventCommand(
        title = payload.title,
        location = payload.location,
        startTime = payload.startTime,
        endTime = payload.endTime,
        timezone = payload.timezone,
        visibility = payload.visibility,
    )
}
