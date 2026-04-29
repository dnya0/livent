package com.livent.event.adapter.outbound.persistence.entity

import java.time.Instant
import com.livent.event.domain.type.EventVisibility
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("events")
data class EventEntity(
    @Id
    val id: Long? = null,
    val title: String,
    val location: String,
    val startTime: Instant,
    val endTime: Instant,
    val timezone: String,
    val visibility: EventVisibility,
)
