package com.livent.event.domain.model.value

@JvmInline
value class EventId private constructor(
    val value: Long,
) {
    companion object {
        fun of(raw: Long): EventId {
            require(raw > 0) { "이벤트 식별자는 0보다 커야 합니다." }
            return EventId(raw)
        }
    }

    override fun toString(): String = value.toString()
}
