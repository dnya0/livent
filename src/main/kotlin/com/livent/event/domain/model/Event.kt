package com.livent.event.domain.model

import java.time.Instant
import com.livent.event.domain.model.type.EventVisibility
import com.livent.event.domain.model.value.EventId
import com.livent.event.domain.model.value.EventDetails
import com.livent.event.domain.model.value.EventTimezone

data class Event(
    val id: EventId,
    val details: EventDetails,
) {
    val title get() = details.title
    val location get() = details.location
    val schedule get() = details.schedule
    val visibility get() = details.visibility

    fun rename(title: String): Event = copy(details = details.rename(title))

    fun relocate(location: String): Event = copy(details = details.relocate(location))

    fun reschedule(startTime: Instant, endTime: Instant, timezone: EventTimezone): Event =
        copy(details = details.reschedule(startTime = startTime, endTime = endTime, timezone = timezone))

    fun changeVisibility(visibility: EventVisibility): Event = copy(details = details.changeVisibility(visibility))
}

data class NewEvent(
    val details: EventDetails,
) {
    val title get() = details.title
    val location get() = details.location
    val schedule get() = details.schedule
    val visibility get() = details.visibility

    fun persist(id: EventId): Event = Event(
        id = id,
        details = details,
    )

    companion object {
        fun create(
            title: String,
            location: String,
            startTime: Instant,
            endTime: Instant,
            timezone: EventTimezone,
            visibility: EventVisibility,
        ): NewEvent = NewEvent(
            details = EventDetails.create(
                title = title,
                location = location,
                startTime = startTime,
                endTime = endTime,
                timezone = timezone,
                visibility = visibility,
            ),
        )
    }
}
