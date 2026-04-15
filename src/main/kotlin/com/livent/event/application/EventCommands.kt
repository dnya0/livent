package com.livent.event.application

import java.time.Instant
import com.livent.event.domain.model.type.EventVisibility

data class CreateEventCommand(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)

data class UpdateEventCommand(
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)
