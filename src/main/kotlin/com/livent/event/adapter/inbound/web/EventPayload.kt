package com.livent.event.adapter.inbound.web

import java.time.Instant
import com.livent.event.domain.type.EventVisibility
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class EventPayload(
    @field:NotBlank(message = "title must not be blank.")
    val title: String,
    @field:NotBlank(message = "location must not be blank.")
    val location: String,
    @field:NotNull(message = "startTime must not be null.")
    val startTime: Instant,
    @field:NotNull(message = "endTime must not be null.")
    val endTime: Instant,
    @field:NotBlank(message = "timezone must not be blank.")
    val timezone: String,
    @field:NotNull(message = "visibility must not be null.")
    val visibility: EventVisibility,
)

data class EventCommandPayload(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)

fun EventPayload.toCommandPayload(): EventCommandPayload = EventCommandPayload(
    title = title,
    location = location,
    startTime = startTime,
    endTime = endTime,
    timezone = timezone,
    visibility = visibility,
)
