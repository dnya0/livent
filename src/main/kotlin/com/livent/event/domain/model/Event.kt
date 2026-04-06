package com.livent.event.domain.model

import java.time.OffsetDateTime
import com.livent.event.domain.model.type.EventVisibility

data class Event(
    val id: Long,
    val title: String,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val visibility: EventVisibility,
)
