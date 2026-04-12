package com.livent.event.domain.model

import com.livent.event.domain.model.type.EventVisibility
import com.livent.event.domain.model.value.EventSchedule

data class Event(
    val id: Long,
    val title: String,
    val location: String,
    val schedule: EventSchedule,
    val visibility: EventVisibility,
)
