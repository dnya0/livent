package com.livent.event.domain.model.value

import java.time.Instant

data class EventSchedule(
    val startTime: Instant,
    val endTime: Instant,
    val timezone: EventTimezone,
) {
    init {
        require(startTime.isBefore(endTime)) { "시작 시각은 종료 시각보다 이전이어야 합니다." }
    }
}
