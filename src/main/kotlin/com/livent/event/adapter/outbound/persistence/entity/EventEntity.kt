package com.livent.event.adapter.outbound.persistence.entity

import java.time.OffsetDateTime
import com.livent.event.domain.model.type.EventVisibility
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("events")
data class EventEntity(
    @Id
    val id: Long? = null,
    val title: String,
    val location: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
    val visibility: EventVisibility,
)
