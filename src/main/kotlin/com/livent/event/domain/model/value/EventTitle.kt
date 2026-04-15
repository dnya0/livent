package com.livent.event.domain.model.value

@JvmInline
value class EventTitle private constructor(
    val value: String,
) {
    companion object {
        fun of(raw: String): EventTitle {
            val normalized = raw.trim()
            require(normalized.isNotEmpty()) { "행사명은 비어 있을 수 없습니다." }
            require(normalized.length <= 255) { "행사명은 255자를 초과할 수 없습니다." }
            return EventTitle(normalized)
        }
    }
}
