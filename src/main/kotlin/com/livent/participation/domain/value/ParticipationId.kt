package com.livent.participation.domain.value

@JvmInline
value class ParticipationId private constructor(
    val value: Long,
) {
    companion object {
        fun of(raw: Long): ParticipationId {
            require(raw > 0) { "참여 식별자는 0보다 커야 합니다." }
            return ParticipationId(raw)
        }
    }
}
