package com.livent.event.domain.model.value

@JvmInline
value class EventLocation private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): EventLocation {
            val normalized = raw.trim()
            require(normalized.isNotEmpty()) { "장소는 비어 있을 수 없습니다." }
            require(normalized.length <= 255) { "장소는 255자를 초과할 수 없습니다." }
            return EventLocation(normalized)
        }
    }
}
