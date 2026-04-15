package com.livent.event.domain.model.value

import java.time.Instant
import com.livent.event.domain.model.type.EventVisibility

data class EventDetails(
    val title: EventTitle,
    val location: EventLocation,
    val schedule: EventSchedule,
    val visibility: EventVisibility,
) {
    fun rename(title: String): EventDetails = copy(title = EventTitle.of(title))

    fun relocate(location: String): EventDetails = copy(location = EventLocation.of(location))

    fun reschedule(
        startTime: Instant,
        endTime: Instant,
        timezone: EventTimezone,
    ): EventDetails = copy(
        schedule = EventSchedule(
            startTime = startTime,
            endTime = endTime,
            timezone = timezone,
        ),
    )

    fun changeVisibility(visibility: EventVisibility): EventDetails = copy(visibility = visibility)

    companion object {
        fun create(
            title: String,
            location: String,
            startTime: Instant,
            endTime: Instant,
            timezone: EventTimezone,
            visibility: EventVisibility,
        ): EventDetails = EventDetails(
            title = EventTitle.of(title),
            location = EventLocation.of(location),
            schedule = EventSchedule(
                startTime = startTime,
                endTime = endTime,
                timezone = timezone,
            ),
            visibility = visibility,
        )
    }
}
