package com.livent.participation.adapter.outbound.persistence.entity

import java.time.Instant
import com.livent.participation.domain.type.ParticipationStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("participations")
data class ParticipationEntity(
    @Id
    val id: Long? = null,
    val userId: Long,
    val eventId: Long,
    val status: ParticipationStatus,
    val joinedAt: Instant,
)
