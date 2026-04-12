package com.livent.event.domain.model.value

import java.time.OffsetDateTime

data class EventSchedule(
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
) {
    init {
        require(startTime < endTime) { "시작 시각은 종료 시각보다 이전이어야 합니다." }
    }
}
