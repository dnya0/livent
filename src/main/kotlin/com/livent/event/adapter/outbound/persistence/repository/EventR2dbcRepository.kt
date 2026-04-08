package com.livent.event.adapter.outbound.persistence.repository

import com.livent.event.adapter.outbound.persistence.entity.EventEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface EventR2dbcRepository : ReactiveCrudRepository<EventEntity, Long> {
    @Query(
        """
        SELECT id, title, location, start_time, end_time, visibility
        FROM events
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findFirstPage(limit: Int): Flux<EventEntity>

    @Query(
        """
        SELECT id, title, location, start_time, end_time, visibility
        FROM events
        WHERE id > :cursor
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findAfterId(cursor: Long, limit: Int): Flux<EventEntity>
}
