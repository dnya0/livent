package com.livent.event.domain.model.value

import java.time.DateTimeException
import java.time.ZoneId

@JvmInline
value class EventTimezone private constructor(
    val value: ZoneId,
) {
    companion object {
        fun of(raw: String): EventTimezone = try {
            EventTimezone(ZoneId.of(raw.trim()))
        } catch (ex: DateTimeException) {
            throw IllegalArgumentException("유효한 IANA timezone 이어야 합니다.", ex)
        }
    }

    val id: String
        get() = value.id
}
